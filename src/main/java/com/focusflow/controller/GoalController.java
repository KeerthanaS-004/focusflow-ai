package com.focusflow.controller;

import com.focusflow.dto.GoalRequest;
import com.focusflow.dto.GoalResponse;
import com.focusflow.dto.TopicRequest;
import com.focusflow.dto.TopicResponse;
import com.focusflow.entity.TopicStatus;
import com.focusflow.service.GoalService;
import com.focusflow.service.TopicService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

	private final GoalService goalService;
	private final TopicService topicService;

	public GoalController(GoalService goalService, TopicService topicService) {
		this.goalService = goalService;
		this.topicService = topicService;
	}

	@PostMapping
	public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request));
	}

	@GetMapping
	public ResponseEntity<List<GoalResponse>> getAll() {
		return ResponseEntity.ok(goalService.getAllForCurrentUser());
	}

	@GetMapping("/{id}")
	public ResponseEntity<GoalResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(goalService.getById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<GoalResponse> update(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
		return ResponseEntity.ok(goalService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		goalService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{goalId}/topics")
	public ResponseEntity<TopicResponse> addTopic(@PathVariable Long goalId, @Valid @RequestBody TopicRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(topicService.addTopicToGoal(goalId, request));
	}

	@PutMapping("/topics/{topicId}")
	public ResponseEntity<TopicResponse> updateTopicStatus(@PathVariable Long topicId, @RequestParam TopicStatus status) {
		return ResponseEntity.ok(topicService.updateStatus(topicId, status));
	}

	@DeleteMapping("/topics/{topicId}")
	public ResponseEntity<Void> deleteTopic(@PathVariable Long topicId) {
		topicService.delete(topicId);
		return ResponseEntity.noContent().build();
	}
}
