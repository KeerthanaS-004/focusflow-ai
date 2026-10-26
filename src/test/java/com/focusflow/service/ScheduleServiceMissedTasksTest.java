package com.focusflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.focusflow.entity.Schedule;
import com.focusflow.entity.ScheduledTask;
import com.focusflow.entity.ScheduledTaskStatus;
import com.focusflow.entity.Task;
import com.focusflow.entity.TaskStatus;
import com.focusflow.entity.User;
import com.focusflow.repository.ScheduleRepository;
import com.focusflow.repository.ScheduledTaskRepository;
import com.focusflow.repository.TaskRepository;
import com.focusflow.service.ai.GeminiScheduleService;
import com.focusflow.service.ai.ScheduleValidator;
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
class ScheduleServiceMissedTasksTest {
	@Mock private GeminiScheduleService geminiScheduleService;
	@Mock private ScheduleValidator scheduleValidator;
	@Mock private ScheduleRepository scheduleRepository;
	@Mock private ScheduledTaskRepository scheduledTaskRepository;
	@Mock private TaskRepository taskRepository;
	@Mock private CurrentUserService currentUserService;
	private ScheduleService service;
	private User user;
	private final Clock fixedClock = Clock.fixed(Instant.parse("2026-10-26T03:30:00Z"), ZoneId.of("Asia/Kolkata"));
	private final LocalDate today = LocalDate.of(2026, 10, 26);

	@BeforeEach
	void setUp() {
		service = new ScheduleService(geminiScheduleService, scheduleValidator, scheduleRepository, scheduledTaskRepository, taskRepository, currentUserService, fixedClock);
		user = withId(new User(), 1L);
	}

	@Test void marksPendingPastSlotAsMissed() {
		mockSchedule(today, st("08:00", "08:30", TaskStatus.PENDING, ScheduledTaskStatus.PLANNED));
		assertEquals(1, service.findMissedTasks(user, today).size());
	}
	@Test void doesNotMarkFutureSlotAsMissed() {
		mockSchedule(today, st("20:00", "20:30", TaskStatus.PENDING, ScheduledTaskStatus.PLANNED));
		assertTrue(service.findMissedTasks(user, today).isEmpty());
	}
	@Test void doesNotMarkCompletedTaskAsMissed() {
		mockSchedule(today, st("08:00", "08:30", TaskStatus.COMPLETED, ScheduledTaskStatus.PLANNED));
		assertTrue(service.findMissedTasks(user, today).isEmpty());
	}
	@Test void doesNotMarkAlreadyMissedSlotAsMissed() {
		mockSchedule(today, st("08:00", "08:30", TaskStatus.PENDING, ScheduledTaskStatus.MISSED));
		assertTrue(service.findMissedTasks(user, today).isEmpty());
	}
	@Test void marksAllPendingSlotsMissedWhenScheduleDateIsPast() {
		mockSchedule(today.minusDays(1), st("20:00", "20:30", TaskStatus.PENDING, ScheduledTaskStatus.PLANNED), st("21:00", "21:30", TaskStatus.IN_PROGRESS, ScheduledTaskStatus.PLANNED));
		assertEquals(2, service.findMissedTasks(user, today.minusDays(1)).size());
	}
	@Test void marksNoSlotsMissedWhenScheduleDateIsFuture() {
		mockSchedule(today.plusDays(1), st("06:00", "06:30", TaskStatus.PENDING, ScheduledTaskStatus.PLANNED));
		assertTrue(service.findMissedTasks(user, today.plusDays(1)).isEmpty());
	}
	@Test void returnsEmptyWhenNoScheduleForDate() {
		when(scheduleRepository.findByUserIdAndScheduleDate(1L, today)).thenReturn(Optional.empty());
		assertTrue(service.findMissedTasks(user, today).isEmpty());
	}

	private void mockSchedule(LocalDate date, ScheduledTask... tasks) {
		Schedule schedule = withId(new Schedule(), 10L); schedule.setUser(user); schedule.setScheduleDate(date);
		when(scheduleRepository.findByUserIdAndScheduleDate(1L, date)).thenReturn(Optional.of(schedule));
		when(scheduledTaskRepository.findByScheduleId(10L)).thenReturn(List.of(tasks));
	}

	private static ScheduledTask st(String start, String end, TaskStatus taskStatus, ScheduledTaskStatus scheduledStatus) {
		Task t = new Task(); t.setStatus(taskStatus);
		ScheduledTask st = new ScheduledTask(); st.setTask(t); st.setStartTime(LocalTime.parse(start)); st.setEndTime(LocalTime.parse(end)); st.setStatus(scheduledStatus);
		return st;
	}

	private static <T> T withId(T entity, Long id) {
		ReflectionTestUtils.setField(entity, "id", id);
		return entity;
	}
}
