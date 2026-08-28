package com.focusflow.dto;

import jakarta.validation.constraints.NotBlank;

public class TopicRequest {

	@NotBlank(message = "Topic name is required")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
