package com.focusflow.scheduler;

import com.focusflow.entity.User;
import com.focusflow.repository.UserRepository;
import com.focusflow.service.ScheduleService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NightlyReplanJob {

	private static final Logger log = LoggerFactory.getLogger(NightlyReplanJob.class);

	private final ScheduleService scheduleService;
	private final UserRepository userRepository;
	private final Clock clock;

	public NightlyReplanJob(ScheduleService scheduleService, UserRepository userRepository, Clock clock) {
		this.scheduleService = scheduleService;
		this.userRepository = userRepository;
		this.clock = clock;
	}

	@Scheduled(cron = "${focusflow.replan.cron:0 0 23 * * *}", zone = "${focusflow.replan.zone:Asia/Kolkata}")
	public void runNightlyReplan() {
		List<User> users = userRepository.findAll();
		int processed = users.size();
		int replanned = 0;
		int failed = 0;

		for (User user : users) {
			try {
				if (scheduleService.replan(user, LocalDate.now(clock)).getNewSchedule() != null) {
					replanned++;
				}
			} catch (Exception ex) {
				// Why: per-user isolation keeps one AI failure from stopping the nightly batch for everyone else.
				failed++;
				log.error("Nightly replan failed for user {}", user.getId(), ex);
			}
		}

		log.info("Nightly replan finished: {} users processed, {} replanned, {} failed", processed, replanned, failed);
	}
}
