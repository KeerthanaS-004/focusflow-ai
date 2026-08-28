package com.focusflow.repository;

import com.focusflow.entity.Task;
import com.focusflow.entity.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserId(Long userId);

	List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
}
