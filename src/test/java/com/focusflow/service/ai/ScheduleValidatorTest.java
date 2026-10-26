package com.focusflow.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.focusflow.dto.ai.AiScheduleResponse;
import com.focusflow.dto.ai.AiScheduledSlot;
import com.focusflow.entity.AvailabilitySlot;
import com.focusflow.entity.Task;
import com.focusflow.entity.User;
import com.focusflow.repository.AvailabilitySlotRepository;
import com.focusflow.repository.TaskRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScheduleValidatorTest {
	@Mock private TaskRepository taskRepository;
	@Mock private AvailabilitySlotRepository availabilitySlotRepository;
	private ScheduleValidator validator;
	private User user;
	private User otherUser;
	private LocalDate today;
	private Task task1;
	private Task task2;
	private Task foreignTask;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(Instant.parse("2026-10-26T03:30:00Z"), ZoneId.of("Asia/Kolkata"));
		validator = new ScheduleValidator(taskRepository, availabilitySlotRepository, fixedClock);
		today = LocalDate.of(2026, 10, 26);
		user = user(1L);
		otherUser = user(2L);
		task1 = task(101L, user, 60);
		task2 = task(102L, user, 30);
		foreignTask = task(201L, otherUser, 45);
		lenient().when(availabilitySlotRepository.findByUserId(1L)).thenReturn(List.of(window("06:00", "08:00"), window("20:00", "22:30")));
		lenient().when(taskRepository.findById(101L)).thenReturn(Optional.of(task1));
		lenient().when(taskRepository.findById(102L)).thenReturn(Optional.of(task2));
		lenient().when(taskRepository.findById(201L)).thenReturn(Optional.of(foreignTask));
	}

	@Test void keepsValidSlot() { assertEquals(1, validate(slot(101L, "20:00", "21:00")).size()); }
	@Test void dropsSlotOutsideAvailability() { assertTrue(validate(slot(101L, "10:00", "11:00")).isEmpty()); }
	@Test void dropsSlotWithWrongDuration() { assertTrue(validate(slot(101L, "20:00", "20:30")).isEmpty()); }
	@Test void dropsOverlappingSlotButKeepsBackToBack() {
		List<AiScheduledSlot> kept = validate(slot(101L, "20:00", "21:00"), slot(102L, "20:30", "21:00"), slot(102L, "21:00", "21:30"));
		assertEquals(2, kept.size());
		assertEquals("20:00", kept.get(0).startTime());
		assertEquals("21:00", kept.get(1).startTime());
	}
	@Test void dropsRepeatedTaskIdKeepsFirst() {
		List<AiScheduledSlot> kept = validate(slot(101L, "20:00", "21:00"), slot(101L, "21:00", "22:00"));
		assertEquals(1, kept.size());
		assertEquals("20:00", kept.get(0).startTime());
	}
	@Test void dropsSlotForAnotherUsersTask() { assertTrue(validate(slot(201L, "20:00", "20:45")).isEmpty()); }
	@Test void dropsMalformedOrInvalidTimeRange() {
		assertTrue(validate(slot(101L, "oops", "21:00")).isEmpty());
		assertTrue(validate(slot(101L, "21:00", "21:00")).isEmpty());
	}
	@Test void dropsPastSlotForTodayKeepsFutureSlotWithFixedClock() {
		List<AiScheduledSlot> kept = validate(slot(101L, "06:00", "07:00"), slot(101L, "20:00", "21:00"));
		assertEquals(1, kept.size());
		assertEquals("20:00", kept.get(0).startTime());
	}
	@Test void dropsInvalidSlotThenAcceptsLaterValidSlotForSameTask() {
		List<AiScheduledSlot> kept = validate(slot(102L, "20:00", "20:45"), slot(102L, "21:00", "21:30"));
		assertEquals(1, kept.size());
		assertEquals("21:00", kept.get(0).startTime());
	}
	@Test void returnsEmptyListForNullResponseOrNullOrEmptySlots() {
		assertTrue(validator.validateAgainstConstraints(null, user, today).isEmpty());
		assertTrue(validator.validateAgainstConstraints(new AiScheduleResponse(null, "x"), user, today).isEmpty());
		assertTrue(validator.validateAgainstConstraints(new AiScheduleResponse(List.of(), "x"), user, today).isEmpty());
	}

	private List<AiScheduledSlot> validate(AiScheduledSlot... slots) {
		return validator.validateAgainstConstraints(new AiScheduleResponse(List.of(slots), "ok"), user, today);
	}

	private static AiScheduledSlot slot(Long taskId, String start, String end) { return new AiScheduledSlot(taskId, start, end, "r"); }
	private static AvailabilitySlot window(String start, String end) { AvailabilitySlot s = new AvailabilitySlot(); s.setStartTime(LocalTime.parse(start)); s.setEndTime(LocalTime.parse(end)); return s; }
	private static User user(Long id) { User u = withId(new User(), id); u.setName("U" + id); u.setEmail("u" + id + "@x.com"); u.setPassword("p"); return u; }
	private static Task task(Long id, User owner, int mins) { Task t = withId(new Task(), id); t.setUser(owner); t.setTitle("T" + id); t.setEstMinutes(mins); return t; }
	private static <T> T withId(T entity, Long id) { ReflectionTestUtils.setField(entity, "id", id); return entity; }
}
