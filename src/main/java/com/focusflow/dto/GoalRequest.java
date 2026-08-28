package com.focusflow.dto;

import com.focusflow.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class GoalRequest {

	@NotBlank(message = "Title is required")
	private String title;

	private String description;

	@NotBlank(message = "Category is required")
	private String category;

	private Priority priority;

	private LocalDate targetDate;

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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(LocalDate targetDate) {
		this.targetDate = targetDate;
	}
}
