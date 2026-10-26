package com.focusflow.dto;

import com.focusflow.entity.ScheduleStatus;
import java.time.LocalDate;
import java.util.List;

public class ScheduleResponse {

	private final Long id;
	private final LocalDate scheduleDate;
	private final ScheduleStatus status;
	private final String explanation;
	private final List<ScheduledTaskResponse> scheduledTasks;

	public ScheduleResponse(
		Long id,
		LocalDate scheduleDate,
		ScheduleStatus status,
		String explanation,
		List<ScheduledTaskResponse> scheduledTasks
	) {
		this.id = id;
		this.scheduleDate = scheduleDate;
		this.status = status;
		this.explanation = explanation;
		this.scheduledTasks = scheduledTasks;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getScheduleDate() {
		return scheduleDate;
	}

	public ScheduleStatus getStatus() {
		return status;
	}

	public String getExplanation() {
		return explanation;
	}

	public List<ScheduledTaskResponse> getScheduledTasks() {
		return scheduledTasks;
	}
}
