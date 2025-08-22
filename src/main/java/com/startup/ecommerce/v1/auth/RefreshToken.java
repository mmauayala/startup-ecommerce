package com.startup.ecommerce.v1.auth;

import com.startup.ecommerce.v1.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens", indexes = {
		@Index(name = "idx_refresh_token", columnList = "token", unique = true)
})
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 200)
	private String token;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	@Builder.Default
	private boolean revoked = false;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
	private User user;
}