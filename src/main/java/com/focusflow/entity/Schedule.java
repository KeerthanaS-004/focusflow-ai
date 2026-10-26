package com.focusflow.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
public class Schedule extends BaseEntity {

	@Column(nullable = false)
	private LocalDate scheduleDate;

	@Column(length = 1000)
	private String explanation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ScheduleStatus status = ScheduleStatus.DRAFT;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ScheduledTask> scheduledTasks = new ArrayList<>();

	@PrePersist
	void applyDefaults() {
		if (status == null) {
			status = ScheduleStatus.DRAFT;
		}
	}

	public LocalDate getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(LocalDate scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public ScheduleStatus getStatus() {
		return status;
	}

	public void setStatus(ScheduleStatus status) {
		this.status = status;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<ScheduledTask> getScheduledTasks() {
		return scheduledTasks;
	}

	public void setScheduledTasks(List<ScheduledTask> scheduledTasks) {
		this.scheduledTasks = scheduledTasks;
	}
}
