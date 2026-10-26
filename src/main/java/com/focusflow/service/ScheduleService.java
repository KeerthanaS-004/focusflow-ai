package com.focusflow.service;

import com.focusflow.dto.ReplanResponse;
import com.focusflow.dto.ScheduleResponse;
import com.focusflow.dto.ScheduledTaskResponse;
import com.focusflow.dto.TaskResponse;
import com.focusflow.dto.ai.AiScheduleResponse;
import com.focusflow.dto.ai.AiScheduledSlot;
import com.focusflow.entity.Schedule;
import com.focusflow.entity.ScheduleStatus;
import com.focusflow.entity.ScheduledTask;
import com.focusflow.entity.ScheduledTaskStatus;
import com.focusflow.entity.Task;
import com.focusflow.entity.TaskStatus;
import com.focusflow.entity.User;
import com.focusflow.exception.ResourceNotFoundException;
import com.focusflow.repository.ScheduleRepository;
import com.focusflow.repository.ScheduledTaskRepository;
import com.focusflow.repository.TaskRepository;
import com.focusflow.service.ai.GeminiScheduleService;
import com.focusflow.service.ai.ScheduleValidator;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

	private final GeminiScheduleService geminiScheduleService;
	private final ScheduleValidator scheduleValidator;
	private final ScheduleRepository scheduleRepository;
	private final ScheduledTaskRepository scheduledTaskRepository;
	private final TaskRepository taskRepository;
	private final CurrentUserService currentUserService;
	private final Clock clock;

	public ScheduleService(
		GeminiScheduleService geminiScheduleService,
		ScheduleValidator scheduleValidator,
		ScheduleRepository scheduleRepository,
		ScheduledTaskRepository scheduledTaskRepository,
		TaskRepository taskRepository,
		CurrentUserService currentUserService,
		Clock clock
	) {
		this.geminiScheduleService = geminiScheduleService;
		this.scheduleValidator = scheduleValidator;
		this.scheduleRepository = scheduleRepository;
		this.scheduledTaskRepository = scheduledTaskRepository;
		this.taskRepository = taskRepository;
		this.currentUserService = currentUserService;
		this.clock = clock;
	}

	public ScheduleResponse generateForDate(LocalDate date) {
		User currentUser = currentUserService.getCurrentUser();
		return generateForUser(currentUser, date);
	}

	public ScheduleResponse generateForUser(User user, LocalDate date) {
		AiScheduleResponse aiSchedule = geminiScheduleService.generateSchedule(user, date);
		List<AiScheduledSlot> validSlots = scheduleValidator.validateAgainstConstraints(aiSchedule, user, date);
		Schedule schedule = getOrCreateSchedule(user, date);
		clearScheduledTasks(schedule);
		persistScheduledTasks(schedule, validSlots);
		schedule.setExplanation(aiSchedule.explanation());
		scheduleRepository.save(schedule);
		return toResponse(schedule);
	}

	public List<ScheduledTask> findMissedTasks(User user, LocalDate date) {
		// Why: deterministic detection isolates missed-task logic from AI reasoning and keeps the policy predictable.
		Schedule schedule = scheduleRepository.findByUserIdAndScheduleDate(user.getId(), date).orElse(null);
		if (schedule == null) {
			return List.of();
		}
		LocalDate today = LocalDate.now(clock);
		LocalTime now = LocalTime.now(clock);
		return scheduledTaskRepository.findByScheduleId(schedule.getId()).stream()
			.filter(taskEntry -> taskEntry.getStatus() == ScheduledTaskStatus.PLANNED)
			.filter(taskEntry -> isMissedTask(taskEntry, date, today, now))
			.toList();
	}

	public ReplanResponse replan(LocalDate missedDate) {
		User currentUser = currentUserService.getCurrentUser();
		return replan(currentUser, missedDate);
	}

	@Transactional
	public ReplanResponse replan(User user, LocalDate missedDate) {
		List<ScheduledTask> missedSlots = findMissedTasks(user, missedDate);
		if (missedSlots.isEmpty()) {
			// Why: early return stops pointless Gemini calls when there is nothing missed to replan.
			return new ReplanResponse(List.of(), null, "Nothing to replan — no missed tasks.");
		}

		List<Task> missedTasks = missedSlots.stream().map(ScheduledTask::getTask).toList();
		LocalDate targetDate = missedDate.plusDays(1);
		AiScheduleResponse aiSchedule = geminiScheduleService.generateSchedule(user, targetDate, missedTasks);
		List<AiScheduledSlot> validSlots = scheduleValidator.validateAgainstConstraints(aiSchedule, user, targetDate);
		Schedule schedule = getOrCreateSchedule(user, targetDate);
		clearScheduledTasks(schedule);
		persistScheduledTasks(schedule, validSlots);
		schedule.setExplanation(aiSchedule.explanation());
		scheduleRepository.save(schedule);

		// Why: marking only after the new schedule is saved keeps the earlier missed-slot record atomic with the replan.
		for (ScheduledTask scheduledTask : missedSlots) {
			scheduledTask.setStatus(ScheduledTaskStatus.MISSED);
			scheduledTaskRepository.save(scheduledTask);
		}

		List<TaskResponse> missedTaskResponses = missedTasks.stream().map(this::toTaskResponse).toList();
		return new ReplanResponse(missedTaskResponses, toResponse(schedule), aiSchedule.explanation());
	}

	public ScheduleResponse getByDate(LocalDate date) {
		User currentUser = currentUserService.getCurrentUser();
		Schedule schedule = scheduleRepository.findByUserIdAndScheduleDate(currentUser.getId(), date)
			.orElseThrow(() -> new ResourceNotFoundException("Schedule not found for date: " + date));
		return toResponse(schedule);
	}

	private ScheduleResponse toResponse(Schedule schedule) {
		List<ScheduledTaskResponse> tasks = scheduledTaskRepository.findByScheduleId(schedule.getId()).stream()
			.sorted(Comparator.comparing(ScheduledTask::getStartTime))
			.map(this::mapScheduledTask)
			.toList();
		return new ScheduleResponse(
			schedule.getId(),
			schedule.getScheduleDate(),
			schedule.getStatus(),
			schedule.getExplanation(),
			tasks
		);
	}

	private ScheduledTaskResponse mapScheduledTask(ScheduledTask scheduledTask) {
		Task task = scheduledTask.getTask();
		return new ScheduledTaskResponse(
			task.getId(),
			task.getTitle(),
			scheduledTask.getStartTime(),
			scheduledTask.getEndTime(),
			scheduledTask.getStatus()
		);
	}

	private TaskResponse toTaskResponse(Task task) {
		return new TaskResponse(
			task.getId(),
			task.getTitle(),
			task.getDescription(),
			task.getEstMinutes(),
			task.getPriority(),
			task.getDeadline(),
			task.getStatus(),
			task.getGoal() != null ? task.getGoal().getId() : null,
			task.getTopic() != null ? task.getTopic().getId() : null
		);
	}

	private Schedule getOrCreateSchedule(User currentUser, LocalDate date) {
		return scheduleRepository.findByUserIdAndScheduleDate(currentUser.getId(), date)
			.orElseGet(() -> {
				Schedule newSchedule = new Schedule();
				newSchedule.setUser(currentUser);
				newSchedule.setScheduleDate(date);
				newSchedule.setStatus(ScheduleStatus.DRAFT);
				return scheduleRepository.save(newSchedule);
			});
	}

	private void clearScheduledTasks(Schedule schedule) {
		List<ScheduledTask> existingTasks = scheduledTaskRepository.findByScheduleId(schedule.getId());
		if (!existingTasks.isEmpty()) {
			scheduledTaskRepository.deleteAll(existingTasks);
		}
		schedule.setStatus(ScheduleStatus.DRAFT);
	}

	private void persistScheduledTasks(Schedule schedule, List<AiScheduledSlot> validSlots) {
		for (AiScheduledSlot slot : validSlots) {
			Task task = taskRepository.findById(slot.taskId())
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + slot.taskId()));
			ScheduledTask scheduledTask = new ScheduledTask();
			scheduledTask.setSchedule(schedule);
			scheduledTask.setTask(task);
			scheduledTask.setStartTime(LocalTime.parse(slot.startTime()));
			scheduledTask.setEndTime(LocalTime.parse(slot.endTime()));
			scheduledTask.setStatus(ScheduledTaskStatus.PLANNED);
			scheduledTaskRepository.save(scheduledTask);
		}
	}

	private boolean isMissedTask(ScheduledTask scheduledTask, LocalDate date, LocalDate today, LocalTime now) {
		Task task = scheduledTask.getTask();
		if (scheduledTask.getStatus() != ScheduledTaskStatus.PLANNED) {
			return false;
		}
		if (task == null || (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.IN_PROGRESS)) {
			return false;
		}
		if (date.isBefore(today)) {
			return true;
		}
		if (date.isEqual(today)) {
			return scheduledTask.getEndTime().isBefore(now);
		}
		return false;
	}
}
