package com.focusflow.service;

import com.focusflow.entity.User;
import com.focusflow.exception.UnauthorizedAccessException;
import com.focusflow.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UnauthorizedAccessException("User is not authenticated");
		}

		Object principal = authentication.getPrincipal();
		String email = extractEmail(principal);
		return userRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedAccessException("Authenticated user not found"));
	}

	private String extractEmail(Object principal) {
		if (principal instanceof UserDetails userDetails) {
			return userDetails.getUsername();
		}
		if (principal instanceof String principalName && !"anonymousUser".equals(principalName)) {
			return principalName;
		}
		throw new UnauthorizedAccessException("User is not authenticated");
	}
}
