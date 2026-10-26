package com.focusflow.controller;

import com.focusflow.dto.ReplanResponse;
import com.focusflow.dto.ScheduleResponse;
import com.focusflow.service.ScheduleService;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

	private final ScheduleService scheduleService;

	public ScheduleController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@PostMapping("/generate")
	public ResponseEntity<ScheduleResponse> generate(@RequestParam("date") LocalDate date) {
		return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.generateForDate(date));
	}

	@PostMapping("/replan")
	public ResponseEntity<ReplanResponse> replan(@RequestParam("date") LocalDate date) {
		return ResponseEntity.ok(scheduleService.replan(date));
	}

	@GetMapping
	public ResponseEntity<ScheduleResponse> getByDate(@RequestParam("date") LocalDate date) {
		return ResponseEntity.ok(scheduleService.getByDate(date));
	}
}
