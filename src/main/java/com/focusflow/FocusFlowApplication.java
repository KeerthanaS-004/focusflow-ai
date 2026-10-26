package com.focusflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FocusFlowApplication {
	public static void main(String[] args) {
		SpringApplication.run(FocusFlowApplication.class, args);
	}
}
