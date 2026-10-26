package com.focusflow.service.ai;

import com.focusflow.dto.ai.AiScheduledSlot;
import com.focusflow.dto.ai.AiScheduleResponse;
import com.focusflow.entity.AvailabilitySlot;
import com.focusflow.entity.Task;
import com.focusflow.entity.User;
import com.focusflow.exception.ResourceNotFoundException;
import com.focusflow.exception.UnauthorizedAccessException;
import com.focusflow.repository.AvailabilitySlotRepository;
import com.focusflow.repository.TaskRepository;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScheduleValidator {

	private static final Logger log = LoggerFactory.getLogger(ScheduleValidator.class);

	private final TaskRepository taskRepository;
	private final AvailabilitySlotRepository availabilitySlotRepository;
	private final Clock clock;

	public ScheduleValidator(
		TaskRepository taskRepository,
		AvailabilitySlotRepository availabilitySlotRepository,
		Clock clock
	) {
		this.taskRepository = taskRepository;
		this.availabilitySlotRepository = availabilitySlotRepository;
		this.clock = clock;
	}

	public List<AiScheduledSlot> validateAgainstConstraints(AiScheduleResponse response, User user, LocalDate targetDate) {
		if (response == null || response.slots() == null || response.slots().isEmpty()) {
			return List.of();
		}

		// Why: LLMs are creative but not reliable; defensive validation drops malformed or out-of-scope slots before they can poison the schedule.
		List<AvailabilitySlot> availabilitySlots = availabilitySlotRepository.findByUserId(user.getId());
		List<AiScheduledSlot> validSlots = new ArrayList<>();
		Set<Long> seenTaskIds = new HashSet<>();
		for (AiScheduledSlot slot : response.slots()) {
			try {
				if (slot == null) {
					throw new IllegalArgumentException("Slot is null");
				}

				Task task = validateOwnership(slot.taskId(), user);
				LocalTime startTime = parseTime(slot.startTime(), "startTime");
				LocalTime endTime = parseTime(slot.endTime(), "endTime");
				// Why: for same-day plans, past-start slots are already impossible and should be dropped before persistence.
				if (targetDate.isEqual(LocalDate.now(clock)) && startTime.isBefore(LocalTime.now(clock))) {
					throw new IllegalArgumentException("startTime is before current time for task " + task.getId());
				}
				if (!startTime.isBefore(endTime)) {
					throw new IllegalArgumentException("startTime must be before endTime for task " + task.getId());
				}
				if (!isWithinAvailability(availabilitySlots, startTime, endTime)) {
					throw new IllegalArgumentException("Slot falls outside user availability for task " + task.getId());
				}

				// Why: duration must match the task estimate to prevent phantom work or mis-sized tasks from entering the schedule.
				if (java.time.Duration.between(startTime, endTime).toMinutes() != task.getEstMinutes()) {
					throw new IllegalArgumentException("Slot duration must match task estimate for task " + task.getId());
				}

				// Why: repeated task IDs would create duplicate work entries in the same schedule; each AI task should be scheduled once.
				if (!seenTaskIds.add(task.getId())) {
					throw new IllegalArgumentException("Duplicate task id " + task.getId() + " in AI response");
				}

				// Why: overlapping slots create impossible double-booking, so earlier accepted slots win and later conflicting entries are dropped.
				if (hasOverlap(validSlots, startTime, endTime)) {
					throw new IllegalArgumentException("Slot overlaps an accepted slot for task " + task.getId());
				}

				validSlots.add(slot);
			} catch (RuntimeException ex) {
				log.warn("Dropping invalid AI-generated slot for user {}: {}", user.getId(), ex.getMessage());
			}
		}
		return validSlots;
	}

	private Task validateOwnership(Long taskId, User user) {
		Task task = taskRepository.findById(taskId)
			.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
		if (!task.getUser().getId().equals(user.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to access this task");
		}
		return task;
	}

	private LocalTime parseTime(String value, String fieldName) {
		try {
			return LocalTime.parse(value);
		} catch (DateTimeException ex) {
			throw new IllegalArgumentException(fieldName + " is not in HH:mm format: " + value, ex);
		}
	}

    private boolean isWithinAvailability(List<AvailabilitySlot> availabilitySlots, LocalTime startTime, LocalTime endTime) {
    	return availabilitySlots.stream()
    		.anyMatch(slot -> !startTime.isBefore(slot.getStartTime()) && !endTime.isAfter(slot.getEndTime()));
    }

    private boolean hasOverlap(List<AiScheduledSlot> validSlots, LocalTime startTime, LocalTime endTime) {
    	return validSlots.stream().anyMatch(existingSlot -> {
    		LocalTime existingStart = LocalTime.parse(existingSlot.startTime());
    		LocalTime existingEnd = LocalTime.parse(existingSlot.endTime());
    		return startTime.isBefore(existingEnd) && existingStart.isBefore(endTime);
    	});
    }
}
