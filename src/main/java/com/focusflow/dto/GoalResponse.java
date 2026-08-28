package com.focusflow.dto;

import com.focusflow.entity.GoalStatus;
import com.focusflow.entity.Priority;
import java.time.LocalDate;
import java.util.List;

public class GoalResponse {

	private final Long id;
	private final String title;
	private final String description;
	private final String category;
	private final Priority priority;
	private final LocalDate targetDate;
	private final int progressPct;
	private final GoalStatus status;
	private final List<TopicResponse> topics;

	public GoalResponse(
		Long id,
		String title,
		String description,
		String category,
		Priority priority,
		LocalDate targetDate,
		int progressPct,
		GoalStatus status,
		List<TopicResponse> topics
	) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.category = category;
		this.priority = priority;
		this.targetDate = targetDate;
		this.progressPct = progressPct;
		this.status = status;
		this.topics = topics;
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

	public String getCategory() {
		return category;
	}

	public Priority getPriority() {
		return priority;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public int getProgressPct() {
		return progressPct;
	}

	public GoalStatus getStatus() {
		return status;
	}

	public List<TopicResponse> getTopics() {
		return topics;
	}
}
