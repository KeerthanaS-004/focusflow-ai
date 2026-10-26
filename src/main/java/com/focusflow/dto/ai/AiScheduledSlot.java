package com.focusflow.dto.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AiScheduledSlot(
	@JsonPropertyDescription("The task ID in FocusFlow that this time block is assigned to. It must match an existing task owned by the current user.")
	Long taskId,
	@JsonPropertyDescription("The start time for this scheduled block in 24-hour HH:mm format, such as 09:00.")
	String startTime,
	@JsonPropertyDescription("The end time for this scheduled block in 24-hour HH:mm format, such as 10:30.")
	String endTime,
	@JsonPropertyDescription("A single-sentence explanation of why this time slot was chosen for the task.")
	String reasoning
) {}
