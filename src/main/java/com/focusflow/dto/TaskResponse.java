package com.focusflow.dto;

import com.focusflow.entity.Priority;
import com.focusflow.entity.TaskStatus;
import java.time.LocalDateTime;

public class TaskResponse {

	private final Long id;
	private final String title;
	private final String description;
	private final int estMinutes;
	private final Priority priority;
	private final LocalDateTime deadline;
	private final TaskStatus status;
	private final Long goalId;
	private final Long topicId;

	public TaskResponse(
		Long id,
		String title,
		String description,
		int estMinutes,
		Priority priority,
		LocalDateTime deadline,
		TaskStatus status,
		Long goalId,
		Long topicId
	) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.estMinutes = estMinutes;
		this.priority = priority;
		this.deadline = deadline;
		this.status = status;
		this.goalId = goalId;
		this.topicId = topicId;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getEstMinutes() {
		return estMinutes;
	}

	public Priority getPriority() {
		return priority;
	}

	public LocalDateTime getDeadline() {
		return deadline;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public Long getGoalId() {
		return goalId;
	}

	public Long getTopicId() {
		return topicId;
	}
}
