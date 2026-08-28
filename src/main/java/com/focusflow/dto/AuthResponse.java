package com.focusflow.dto;

public class AuthResponse {

	private final String token;
	private final String email;
	private final String name;

	public AuthResponse(String token, String email, String name) {
		this.token = token;
		this.email = email;
		this.name = name;
	}

	public String getToken() {
		return token;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}
}
