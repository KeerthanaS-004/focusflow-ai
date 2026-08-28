package com.focusflow.dto;

import com.focusflow.entity.TopicStatus;

public class TopicResponse {

	private final Long id;
	private final String name;
	private final TopicStatus status;

	public TopicResponse(Long id, String name, TopicStatus status) {
		this.id = id;
		this.name = name;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public TopicStatus getStatus() {
		return status;
	}
}
