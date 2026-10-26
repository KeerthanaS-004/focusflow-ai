package com.focusflow.service.ai;

import com.focusflow.entity.AvailabilitySlot;
import com.focusflow.entity.Priority;
import com.focusflow.entity.Schedule;
import com.focusflow.entity.ScheduledTask;
import com.focusflow.entity.Task;
import com.focusflow.entity.TaskStatus;
import com.focusflow.entity.User;
import com.focusflow.repository.ScheduleRepository;
import com.focusflow.repository.ScheduledTaskRepository;
import com.focusflow.repository.AvailabilitySlotRepository;
import com.focusflow.repository.TaskRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ScheduleContextBuilder {

	private final AvailabilitySlotRepository availabilitySlotRepository;
	private final TaskRepository taskRepository;
	private final ScheduleRepository scheduleRepository;
	private final ScheduledTaskRepository scheduledTaskRepository;
	private final Clock clock;

	public ScheduleContextBuilder(
		AvailabilitySlotRepository availabilitySlotRepository,
		TaskRepository taskRepository,
		ScheduleRepository scheduleRepository,
		ScheduledTaskRepository scheduledTaskRepository,
		Clock clock
	) {
		this.availabilitySlotRepository = availabilitySlotRepository;
		this.taskRepository = taskRepository;
		this.scheduleRepository = scheduleRepository;
		this.scheduledTaskRepository = scheduledTaskRepository;
		this.clock = clock;
	}

	public String buildContext(User user, LocalDate targetDate) {
		return buildContext(user, targetDate, List.of());
	}

	public String buildContext(User user, LocalDate targetDate, List<Task> missedTasks) {
		List<AvailabilitySlot> availabilitySlots = availabilitySlotRepository.findByUserIdOrderByStartTimeAsc(user.getId());
		List<Task> tasks = taskRepository.findByUserId(user.getId()).stream()
			.filter(task -> task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.IN_PROGRESS)
			.sorted(Comparator.comparingInt(task -> priorityRank(task.getPriority())))
			.toList();

		StringBuilder builder = new StringBuilder();
		appendCurrentTimeGuidance(builder, targetDate);
		builder.append("User availability:\n");
		if (availabilitySlots.isEmpty()) {
			builder.append("- No availability windows found.\n");
		} else {
			for (AvailabilitySlot slot : availabilitySlots) {
				builder.append("- ")
					.append(slot.getLabel() == null ? "Availability" : slot.getLabel())
					.append(": ")
					.append(slot.getStartTime())
					.append(" to ")
					.append(slot.getEndTime())
					.append("\n");
			}
		}

		builder.append("\nTasks to schedule:\n");
		if (tasks.isEmpty()) {
			builder.append("- No pending or in-progress tasks found.\n");
		} else {
			for (Task task : tasks) {
				String goalTitle = task.getGoal() != null ? task.getGoal().getTitle() : "No goal";
				String deadline = task.getDeadline() != null ? task.getDeadline().toLocalDate().toString() : "No deadline";
				builder.append("- id=")
					.append(task.getId())
					.append(", title=")
					.append(task.getTitle())
					.append(", estMinutes=")
					.append(task.getEstMinutes())
					.append(", priority=")
					.append(task.getPriority())
					.append(", deadline=")
					.append(deadline)
					.append(", goal=")
					.append(goalTitle)
					.append("\n");
			}
		}

		if (missedTasks != null && !missedTasks.isEmpty()) {
			builder.append("\nMissed earlier today (reschedule these with higher urgency):\n");
			for (Task missedTask : missedTasks) {
				builder.append("- id=")
					.append(missedTask.getId())
					.append(", title=")
					.append(missedTask.getTitle())
					.append("\n");
			}
		}
		appendExistingPlan(builder, user, targetDate);

		return builder.toString();
	}

	private void appendCurrentTimeGuidance(StringBuilder builder, LocalDate targetDate) {
		if (!targetDate.isEqual(LocalDate.now(clock))) {
			return;
		}
		String currentTime = LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"));
		builder.append("Current time: ").append(currentTime).append(". Do not schedule anything that starts before this time.\n\n");
	}

	private void appendExistingPlan(StringBuilder builder, User user, LocalDate targetDate) {
		Schedule existingSchedule = scheduleRepository.findByUserIdAndScheduleDate(user.getId(), targetDate).orElse(null);
		if (existingSchedule == null) {
			return;
		}
		List<ScheduledTask> existingTasks = scheduledTaskRepository.findByScheduleId(existingSchedule.getId());
		if (existingTasks.isEmpty()) {
			return;
		}

		builder.append("\nExisting plan for this date (you are replacing it):\n");
		for (ScheduledTask scheduledTask : existingTasks.stream().sorted(Comparator.comparing(ScheduledTask::getStartTime)).toList()) {
			builder.append("- ")
				.append(scheduledTask.getTask().getTitle())
				.append(" (id=")
				.append(scheduledTask.getTask().getId())
				.append(") ")
				.append(scheduledTask.getStartTime())
				.append("-")
				.append(scheduledTask.getEndTime())
				.append("\n");
		}
	}

	private int priorityRank(Priority priority) {
		if (priority == null) {
			return 3;
		}
		return switch (priority) {
			case HIGH -> 0;
			case MEDIUM -> 1;
			case LOW -> 2;
		};
	}
}
