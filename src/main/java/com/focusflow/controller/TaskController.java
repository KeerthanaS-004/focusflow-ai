package com.focusflow.controller;

import com.focusflow.dto.TaskRequest;
import com.focusflow.dto.TaskResponse;
import com.focusflow.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
	}

	@GetMapping
	public ResponseEntity<List<TaskResponse>> getAll() {
		return ResponseEntity.ok(taskService.getAllForCurrentUser());
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(taskService.getById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
		return ResponseEntity.ok(taskService.update(id, request));
	}

	@PatchMapping("/{id}/complete")
	public ResponseEntity<TaskResponse> markComplete(@PathVariable Long id) {
		return ResponseEntity.ok(taskService.markComplete(id));
	}

	@PatchMapping("/{id}/skip")
	public ResponseEntity<TaskResponse> markSkipped(@PathVariable Long id) {
		return ResponseEntity.ok(taskService.markSkipped(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		taskService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
