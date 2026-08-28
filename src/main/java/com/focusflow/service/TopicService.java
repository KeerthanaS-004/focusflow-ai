package com.focusflow.service;

import com.focusflow.dto.TopicRequest;
import com.focusflow.dto.TopicResponse;
import com.focusflow.entity.Goal;
import com.focusflow.entity.Topic;
import com.focusflow.entity.TopicStatus;
import com.focusflow.entity.User;
import com.focusflow.exception.ResourceNotFoundException;
import com.focusflow.exception.UnauthorizedAccessException;
import com.focusflow.repository.GoalRepository;
import com.focusflow.repository.TopicRepository;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

	private final TopicRepository topicRepository;
	private final GoalRepository goalRepository;
	private final CurrentUserService currentUserService;

	public TopicService(
		TopicRepository topicRepository,
		GoalRepository goalRepository,
		CurrentUserService currentUserService
	) {
		this.topicRepository = topicRepository;
		this.goalRepository = goalRepository;
		this.currentUserService = currentUserService;
	}

	public TopicResponse addTopicToGoal(Long goalId, TopicRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Goal goal = getOwnedGoal(goalId, currentUser);
		Topic topic = new Topic();
		topic.setName(request.getName());
		topic.setGoal(goal);
		return toResponse(topicRepository.save(topic));
	}

	public TopicResponse updateStatus(Long topicId, TopicStatus status) {
		User currentUser = currentUserService.getCurrentUser();
		Topic topic = getOwnedTopic(topicId, currentUser);
		topic.setStatus(status);
		return toResponse(topicRepository.save(topic));
	}

	public void delete(Long topicId) {
		User currentUser = currentUserService.getCurrentUser();
		Topic topic = getOwnedTopic(topicId, currentUser);
		topicRepository.delete(topic);
	}

	private Goal getOwnedGoal(Long goalId, User currentUser) {
		Goal goal = goalRepository.findById(goalId)
			.orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));
		if (!goal.getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to modify this goal");
		}
		return goal;
	}

	private Topic getOwnedTopic(Long topicId, User currentUser) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));
		if (!topic.getGoal().getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedAccessException("You are not allowed to modify this topic");
		}
		return topic;
	}

	private TopicResponse toResponse(Topic topic) {
		return new TopicResponse(topic.getId(), topic.getName(), topic.getStatus());
	}
}
