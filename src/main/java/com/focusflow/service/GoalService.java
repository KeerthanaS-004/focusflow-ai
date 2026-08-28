package com.focusflow.service;

import com.focusflow.dto.GoalRequest;
import com.focusflow.dto.GoalResponse;
import com.focusflow.dto.TopicResponse;
import com.focusflow.entity.Goal;
import com.focusflow.entity.Topic;
import com.focusflow.entity.User;
import com.focusflow.exception.ResourceNotFoundException;
import com.focusflow.exception.UnauthorizedAccessException;
import com.focusflow.repository.GoalRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GoalService {

	private final GoalRepository goalRepository;
	private final CurrentUserService currentUserService;

	public GoalService(GoalRepository goalRepository, CurrentUserService currentUserService) {
		this.goalRepository = goalRepository;
		this.currentUserService = currentUserService;
	}

	public GoalResponse create(GoalRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Goal goal = new Goal();
		goal.setTitle(request.getTitle());
		goal.setDescription(request.getDescription());
		goal.setCategory(request.getCategory());
		goal.setPriority(request.getPriority());
		goal.setTargetDate(request.getTargetDate());
		goal.setUser(currentUser);
		return toResponse(goalRepository.save(goal));
	}

	public List<GoalResponse> getAllForCurrentUser() {
		User currentUser = currentUserService.getCurrentUser();
		return goalRepository.findByUserId(currentUser.getId()).stream().map(this::toResponse).toList();
	}

	public GoalResponse getById(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		return toResponse(getOwnedGoal(id, currentUser));
	}

	public GoalResponse update(Long id, GoalRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Goal goal = getOwnedGoal(id, currentUser);
		goal.setTitle(request.getTitle());
		goal.setDescription(request.getDescription());
		goal.setCategory(request.getCategory());
		goal.setPriority(request.getPriority());
		goal.setTargetDate(request.getTargetDate());
		return toResponse(goalRepository.save(goal));
	}

	public void delete(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		Goal goal = getOwnedGoal(id, currentUser);
		goalRepository.delete(goal);
	}

	private Goal getOwnedGoal(Long id, User currentUser) {
		Goal goal = goalRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
		if (!goal.getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to access this goal");
		}
		return goal;
	}

	private GoalResponse toResponse(Goal goal) {
		List<TopicResponse> topics = goal.getTopics().stream().map(this::toTopicResponse).toList();
		return new GoalResponse(
			goal.getId(),
			goal.getTitle(),
			goal.getDescription(),
			goal.getCategory(),
			goal.getPriority(),
			goal.getTargetDate(),
			goal.getProgressPct(),
			goal.getStatus(),
			topics
		);
	}

	private TopicResponse toTopicResponse(Topic topic) {
		return new TopicResponse(topic.getId(), topic.getName(), topic.getStatus());
	}
}
