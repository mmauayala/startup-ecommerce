package com.startup.ecommerce.v1.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
	void deleteByToken(String token);
	long deleteByUser_Id(Long userId);
}