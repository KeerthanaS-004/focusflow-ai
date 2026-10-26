package com.focusflow.service.ai;

import com.focusflow.dto.ai.AiScheduleResponse;
import com.focusflow.entity.Task;
import com.focusflow.entity.User;
import com.focusflow.exception.AiGenerationException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiScheduleService {

	private final ChatClient chatClient;
	private final ScheduleContextBuilder scheduleContextBuilder;

	public GeminiScheduleService(ChatClient.Builder chatClientBuilder, ScheduleContextBuilder scheduleContextBuilder) {
		this.chatClient = chatClientBuilder.build();
		this.scheduleContextBuilder = scheduleContextBuilder;
	}

	public AiScheduleResponse generateSchedule(User user, LocalDate targetDate) {
		return generateSchedule(user, targetDate, List.of());
	}

	public AiScheduleResponse generateSchedule(User user, LocalDate targetDate, List<Task> missedTasks) {
		String contextBlock = scheduleContextBuilder.buildContext(user, targetDate, missedTasks);
		String systemPrompt = "You are a scheduling assistant. Given the user's availability and pending tasks below, assign each task ONLY within the user's available time slots. Respect priority (HIGH tasks placed first/earliest). Do not schedule outside available hours. Do not invent tasks not in the list. Each task may appear at most once. Each slot's length must equal the task's estMinutes exactly. Slots must not overlap each other. Schedule HIGH priority tasks first, but if a higher-priority task does not fit in the remaining time of a window, use that time for a lower-priority task that does fit. If a Missed section is present, give those tasks scheduling preference and explain in the 'explanation' field what you moved. Never place a slot before the current time when a current time is given. If an existing plan is given, the 'explanation' field must say specifically what moved: name each task whose time changed (from -> to), each task newly added or dropped, and say 'unchanged' for the rest. If nothing changed, say so plainly instead of claiming a change. Return ONLY the tasks you can realistically fit — it's fine to leave some unscheduled if there isn't enough time.";
		String userPrompt = "Target date: " + targetDate + "\n\n" + contextBlock;

		// Why: structured output keeps the model response schema strict, which makes AI-generated scheduling data safe to parse and persist.
		try {
			return chatClient.prompt()
				.system(systemPrompt)
				.user(userPrompt)
				.call()
				.entity(AiScheduleResponse.class);
		} catch (Exception ex) {
			throw new AiGenerationException("The AI provider failed while generating a schedule for " + targetDate + ". Please try again later.", ex);
		}
	}
}
