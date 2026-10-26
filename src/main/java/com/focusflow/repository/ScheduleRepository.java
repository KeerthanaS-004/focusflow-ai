package com.focusflow.repository;

import com.focusflow.entity.Schedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	Optional<Schedule> findByUserIdAndScheduleDate(Long userId, LocalDate scheduleDate);

	List<Schedule> findByUserId(Long userId);
}
