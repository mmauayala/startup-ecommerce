package com.startup.ecommerce.v1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
	private final Key signingKey;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration}") long expirationMs
	) {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(secret)));
		this.expirationMs = expirationMs;
	}

	public String generateToken(String subject, Map<String, Object> extraClaims) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
			.setClaims(extraClaims)
			.setSubject(subject)
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(signingKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(signingKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
		return resolver.apply(claims);
	}

	private static String toBase64(String secret) {
		try {
			Decoders.BASE64.decode(secret);
			return secret;
		} catch (Exception ignored) {
			return java.util.Base64.getEncoder().encodeToString(secret.getBytes());
		}
	}
}