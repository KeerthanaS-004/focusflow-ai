package com.focusflow.dto;

import com.focusflow.entity.ScheduledTaskStatus;
import java.time.LocalTime;

public record ScheduledTaskResponse(
	Long taskId,
	String taskTitle,
	LocalTime startTime,
	LocalTime endTime,
	ScheduledTaskStatus status
) {}
