package com.focusflow.service;

import com.focusflow.dto.TaskRequest;
import com.focusflow.dto.TaskResponse;
import com.focusflow.entity.Goal;
import com.focusflow.entity.Task;
import com.focusflow.entity.TaskStatus;
import com.focusflow.entity.Topic;
import com.focusflow.entity.User;
import com.focusflow.exception.ResourceNotFoundException;
import com.focusflow.exception.UnauthorizedAccessException;
import com.focusflow.repository.GoalRepository;
import com.focusflow.repository.TaskRepository;
import com.focusflow.repository.TopicRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

	private final TaskRepository taskRepository;
	private final GoalRepository goalRepository;
	private final TopicRepository topicRepository;
	private final CurrentUserService currentUserService;

	public TaskService(
		TaskRepository taskRepository,
		GoalRepository goalRepository,
		TopicRepository topicRepository,
		CurrentUserService currentUserService
	) {
		this.taskRepository = taskRepository;
		this.goalRepository = goalRepository;
		this.topicRepository = topicRepository;
		this.currentUserService = currentUserService;
	}

	public TaskResponse create(TaskRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = new Task();
		task.setTitle(request.getTitle());
		task.setDescription(request.getDescription());
		task.setEstMinutes(request.getEstMinutes());
		task.setPriority(request.getPriority());
		task.setDeadline(request.getDeadline());
		task.setUser(currentUser);
		task.setGoal(resolveGoal(request.getGoalId(), currentUser));
		task.setTopic(resolveTopic(request.getTopicId(), currentUser));
		return toResponse(taskRepository.save(task));
	}

	public List<TaskResponse> getAllForCurrentUser() {
		User currentUser = currentUserService.getCurrentUser();
		return taskRepository.findByUserId(currentUser.getId()).stream().map(this::toResponse).toList();
	}

	public TaskResponse getById(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		return toResponse(getOwnedTask(id, currentUser));
	}

	public TaskResponse update(Long id, TaskRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = getOwnedTask(id, currentUser);
		task.setTitle(request.getTitle());
		task.setDescription(request.getDescription());
		task.setEstMinutes(request.getEstMinutes());
		task.setPriority(request.getPriority());
		task.setDeadline(request.getDeadline());
		task.setGoal(resolveGoal(request.getGoalId(), currentUser));
		task.setTopic(resolveTopic(request.getTopicId(), currentUser));
		return toResponse(taskRepository.save(task));
	}

	public TaskResponse markComplete(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = getOwnedTask(id, currentUser);
		task.setStatus(TaskStatus.COMPLETED);
		return toResponse(taskRepository.save(task));
	}

	public TaskResponse markSkipped(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = getOwnedTask(id, currentUser);
		task.setStatus(TaskStatus.SKIPPED);
		return toResponse(taskRepository.save(task));
	}

	public void delete(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = getOwnedTask(id, currentUser);
		taskRepository.delete(task);
	}

	private Task getOwnedTask(Long id, User currentUser) {
		Task task = taskRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
		if (!task.getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to access this task");
		}
		return task;
	}

	private Goal resolveGoal(Long goalId, User currentUser) {
		if (goalId == null) {
			return null;
		}

		Goal goal = goalRepository.findById(goalId)
			.orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));
		if (!goal.getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to use this goal");
		}
		return goal;
	}

	private Topic resolveTopic(Long topicId, User currentUser) {
		if (topicId == null) {
			return null;
		}

		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));
		if (!topic.getGoal().getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to use this topic");
		}
		return topic;
	}

	private TaskResponse toResponse(Task task) {
		Long goalId = task.getGoal() != null ? task.getGoal().getId() : null;
		Long topicId = task.getTopic() != null ? task.getTopic().getId() : null;
		return new TaskResponse(
			task.getId(),
			task.getTitle(),
			task.getDescription(),
			task.getEstMinutes(),
			task.getPriority(),
			task.getDeadline(),
			task.getStatus(),
			goalId,
			topicId
		);
	}
}
