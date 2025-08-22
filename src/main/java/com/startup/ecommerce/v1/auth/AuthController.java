package com.startup.ecommerce.v1.auth;

import com.startup.ecommerce.v1.auth.dto.AuthDtos.LoginRequest;
import com.startup.ecommerce.v1.auth.dto.AuthDtos.RefreshRequest;
import com.startup.ecommerce.v1.auth.dto.AuthDtos.RegisterRequest;
import com.startup.ecommerce.v1.auth.dto.AuthDtos.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthController(AuthService authService, RefreshTokenRepository refreshTokenRepository) {
		this.authService = authService;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@PostMapping("/register")
	public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		return ResponseEntity.ok(authService.refresh(request.refreshToken()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(name = "x-refresh-token", required = false) String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).ifPresent(token -> {
				token.setRevoked(true);
				refreshTokenRepository.save(token);
			});
		}
		return ResponseEntity.noContent().build();
	}
}