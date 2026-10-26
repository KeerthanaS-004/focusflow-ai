package com.focusflow.dto.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record AiScheduleResponse(
	@JsonPropertyDescription("The schedule blocks the AI recommends for the target date. Each slot includes the task ID, time window, and a brief rationale.")
	List<AiScheduledSlot> slots,
	@JsonPropertyDescription("One or two sentences that state exactly what changed versus the existing plan: each moved task with from->to time, each newly added or dropped task, and 'unchanged' for the rest; if nothing changed, say that plainly.")
	String explanation
) {}
