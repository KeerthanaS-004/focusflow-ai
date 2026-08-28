package com.focusflow.repository;

import com.focusflow.entity.Topic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {

	List<Topic> findByGoalId(Long goalId);
}
