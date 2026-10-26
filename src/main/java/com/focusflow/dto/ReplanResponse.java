package com.focusflow.dto;

import java.util.List;

public class ReplanResponse {

	private final List<TaskResponse> missedTasks;
	private final ScheduleResponse newSchedule;
	private final String explanation;

	public ReplanResponse(List<TaskResponse> missedTasks, ScheduleResponse newSchedule, String explanation) {
		this.missedTasks = missedTasks;
		this.newSchedule = newSchedule;
		this.explanation = explanation;
	}

	public List<TaskResponse> getMissedTasks() {
		return missedTasks;
	}

	public ScheduleResponse getNewSchedule() {
		return newSchedule;
	}

	public String getExplanation() {
		return explanation;
	}
}
