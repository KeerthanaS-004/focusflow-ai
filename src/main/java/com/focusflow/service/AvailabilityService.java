package com.focusflow.service;

import com.focusflow.dto.AvailabilitySlotRequest;
import com.focusflow.dto.AvailabilitySlotResponse;
import com.focusflow.entity.AvailabilitySlot;
import com.focusflow.entity.User;
import com.focusflow.exception.ResourceNotFoundException;
import com.focusflow.exception.UnauthorizedAccessException;
import com.focusflow.repository.AvailabilitySlotRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

	private final AvailabilitySlotRepository availabilitySlotRepository;
	private final CurrentUserService currentUserService;

	public AvailabilityService(
		AvailabilitySlotRepository availabilitySlotRepository,
		CurrentUserService currentUserService
	) {
		this.availabilitySlotRepository = availabilitySlotRepository;
		this.currentUserService = currentUserService;
	}

	public AvailabilitySlotResponse create(AvailabilitySlotRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		validateTimes(request.getStartTime(), request.getEndTime());
		AvailabilitySlot slot = new AvailabilitySlot();
		slot.setLabel(request.getLabel());
		slot.setStartTime(request.getStartTime());
		slot.setEndTime(request.getEndTime());
		slot.setUser(currentUser);
		return toResponse(availabilitySlotRepository.save(slot));
	}

	public List<AvailabilitySlotResponse> getAllForCurrentUser() {
		User currentUser = currentUserService.getCurrentUser();
		return availabilitySlotRepository.findByUserIdOrderByStartTimeAsc(currentUser.getId())
			.stream()
			.map(this::toResponse)
			.toList();
	}

	public AvailabilitySlotResponse update(Long id, AvailabilitySlotRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		AvailabilitySlot slot = getOwnedAvailabilitySlot(id, currentUser);
		validateTimes(request.getStartTime(), request.getEndTime());
		slot.setLabel(request.getLabel());
		slot.setStartTime(request.getStartTime());
		slot.setEndTime(request.getEndTime());
		return toResponse(availabilitySlotRepository.save(slot));
	}

	public void delete(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		AvailabilitySlot slot = getOwnedAvailabilitySlot(id, currentUser);
		availabilitySlotRepository.delete(slot);
	}

	private AvailabilitySlot getOwnedAvailabilitySlot(Long id, User currentUser) {
		AvailabilitySlot slot = availabilitySlotRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Availability slot not found with id: " + id));
		if (!slot.getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to access this availability slot");
		}
		return slot;
	}

	private void validateTimes(java.time.LocalTime startTime, java.time.LocalTime endTime) {
		if (startTime == null || endTime == null) {
			return;
		}
		if (!startTime.isBefore(endTime)) {
			throw new IllegalArgumentException("Start time must be before end time");
		}
	}

	private AvailabilitySlotResponse toResponse(AvailabilitySlot slot) {
		return new AvailabilitySlotResponse(
			slot.getId(),
			slot.getLabel(),
			slot.getStartTime(),
			slot.getEndTime()
		);
	}
}
