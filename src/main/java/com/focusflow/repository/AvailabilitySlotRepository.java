package com.focusflow.repository;

import com.focusflow.entity.AvailabilitySlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

	List<AvailabilitySlot> findByUserId(Long userId);

	List<AvailabilitySlot> findByUserIdOrderByStartTimeAsc(Long userId);
}
