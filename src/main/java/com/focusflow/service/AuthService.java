package com.focusflow.service;

import com.focusflow.dto.AuthResponse;
import com.focusflow.dto.LoginRequest;
import com.focusflow.dto.RegisterRequest;
import com.focusflow.entity.Role;
import com.focusflow.entity.User;
import com.focusflow.exception.EmailAlreadyExistsException;
import com.focusflow.exception.InvalidCredentialsException;
import com.focusflow.repository.UserRepository;
import com.focusflow.security.JwtService;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		AuthenticationManager authenticationManager
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new EmailAlreadyExistsException("Email is already registered");
		}

		User user = buildUser(request);
		User savedUser = userRepository.save(user);
		String token = jwtService.generateToken(toUserDetails(savedUser));
		return new AuthResponse(token, savedUser.getEmail(), savedUser.getName());
	}

	public AuthResponse login(LoginRequest request) {
		authenticate(request);
		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
		String token = jwtService.generateToken(toUserDetails(user));
		return new AuthResponse(token, user.getEmail(), user.getName());
	}

	private User buildUser(RegisterRequest request) {
		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);
		return user;
	}

	private void authenticate(LoginRequest request) {
		try {
			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
			authenticationManager.authenticate(authToken);
		} catch (AuthenticationException ex) {
			throw new InvalidCredentialsException("Invalid email or password");
		}
	}

	private UserDetails toUserDetails(User user) {
		return new org.springframework.security.core.userdetails.User(
			user.getEmail(),
			user.getPassword(),
			List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
		);
	}
}
