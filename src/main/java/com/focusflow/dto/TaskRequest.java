package com.focusflow.dto;

import com.focusflow.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class TaskRequest {

	@NotBlank(message = "Title is required")
	private String title;

	private String description;

	@Positive(message = "Estimated minutes must be positive")
	private int estMinutes;

	private Priority priority;

	private LocalDateTime deadline;

	private Long goalId;

	private Long topicId;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getEstMinutes() {
		return estMinutes;
	}

	public void setEstMinutes(int estMinutes) {
		this.estMinutes = estMinutes;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public LocalDateTime getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDateTime deadline) {
		this.deadline = deadline;
	}

	public Long getGoalId() {
		return goalId;
	}

	public void setGoalId(Long goalId) {
		this.goalId = goalId;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}
}
