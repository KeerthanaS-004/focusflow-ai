package com.focusflow.controller;

import com.focusflow.dto.AvailabilitySlotRequest;
import com.focusflow.dto.AvailabilitySlotResponse;
import com.focusflow.service.AvailabilityService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

	private final AvailabilityService availabilityService;

	public AvailabilityController(AvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	@PostMapping
	public ResponseEntity<AvailabilitySlotResponse> create(@Valid @RequestBody AvailabilitySlotRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.create(request));
	}

	@GetMapping
	public ResponseEntity<List<AvailabilitySlotResponse>> getAll() {
		return ResponseEntity.ok(availabilityService.getAllForCurrentUser());
	}

	@PutMapping("/{id}")
	public ResponseEntity<AvailabilitySlotResponse> update(
		@PathVariable Long id,
		@Valid @RequestBody AvailabilitySlotRequest request
	) {
		return ResponseEntity.ok(availabilityService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		availabilityService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
