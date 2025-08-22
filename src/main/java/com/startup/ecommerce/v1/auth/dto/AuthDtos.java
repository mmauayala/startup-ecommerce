package com.startup.ecommerce.v1.auth.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {
	public record RegisterRequest(
			@NotBlank @Email String email,
			@NotBlank @Size(min = 6, max = 100) String password,
			@NotBlank String firstName,
			@NotBlank String lastName,
			@Size(max = 20) String phoneNumber,
			@Size(max = 255) String addressLine
	) {}

	public record LoginRequest(
			@NotBlank @Email String email,
			@NotBlank String password
	) {}

	public record TokenResponse(
			String accessToken,
			String refreshToken,
			String tokenType
	) {}

	public record RefreshRequest(
			@NotBlank String refreshToken
	) {}
}