package com.startup.ecommerce.v1.auth;

import com.startup.ecommerce.v1.auth.dto.AuthDtos.LoginRequest;
import com.startup.ecommerce.v1.auth.dto.AuthDtos.RegisterRequest;
import com.startup.ecommerce.v1.auth.dto.AuthDtos.TokenResponse;
import com.startup.ecommerce.v1.user.Role;
import com.startup.ecommerce.v1.user.User;
import com.startup.ecommerce.v1.user.UserRepository;
import com.startup.ecommerce.v1.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthService(UserRepository userRepository,
					 PasswordEncoder passwordEncoder,
					 AuthenticationManager authenticationManager,
					 JwtService jwtService,
					 RefreshTokenRepository refreshTokenRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public TokenResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("Email already in use");
		}
		User user = User.builder()
				.firstName(request.firstName())
				.lastName(request.lastName())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.phoneNumber(request.phoneNumber())
				.addressLine(request.addressLine())
				.role(Role.CUSTOMER)
				.enabled(true)
				.build();
		userRepository.save(user);
		return issueTokens(user);
	}

	public TokenResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.email(), request.password())
			);
		} catch (Exception ex) {
			throw new BadCredentialsException("Invalid credentials");
		}
		User user = userRepository.findByEmail(request.email()).orElseThrow();
		return issueTokens(user);
	}

	public TokenResponse refresh(String refreshToken) {
		RefreshToken token = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
			.orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
		if (token.getExpiresAt().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Refresh token expired");
		}
		User user = token.getUser();
		TokenResponse response = issueTokens(user);
		token.setRevoked(true);
		refreshTokenRepository.save(token);
		return response;
	}

	private TokenResponse issueTokens(User user) {
		String accessToken = jwtService.generateToken(user.getUsername(), Map.of(
				"role", user.getRole().name(),
				"uid", user.getId()
		));
		String newRefresh = UUID.randomUUID().toString();
		RefreshToken refresh = RefreshToken.builder()
				.token(newRefresh)
				.user(user)
				.expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
				.revoked(false)
				.build();
		refreshTokenRepository.save(refresh);
		return new TokenResponse(accessToken, newRefresh, "Bearer");
	}
}