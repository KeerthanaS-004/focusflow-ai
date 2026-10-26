package com.focusflow.dto;

import java.time.LocalTime;

public class AvailabilitySlotResponse {

	private final Long id;
	private final String label;
	private final LocalTime startTime;
	private final LocalTime endTime;

	public AvailabilitySlotResponse(Long id, String label, LocalTime startTime, LocalTime endTime) {
		this.id = id;
		this.label = label;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public Long getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}
}
