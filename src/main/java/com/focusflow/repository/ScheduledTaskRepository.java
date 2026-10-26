package com.focusflow.repository;

import com.focusflow.entity.ScheduledTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

	List<ScheduledTask> findByScheduleId(Long scheduleId);
}
