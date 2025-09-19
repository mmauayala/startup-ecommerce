package com.startup.ecommerce.v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuración para JPA Auditing.
 * Habilita auditoría automática de entidades que extienden BaseAuditEntity.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Bean que proporciona el usuario actual para auditoría.
     * Obtiene el email del usuario autenticado desde el contexto de seguridad.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * Implementación de AuditorAware que obtiene el usuario actual del contexto de seguridad.
     */
    public static class SpringSecurityAuditorAware implements AuditorAware<String> {
        
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system"); // Para casos sin autenticación (seeders, etc.)
            }
            
            // Si el usuario está autenticado, usar su email
            String email = authentication.getName();
            return email != null ? Optional.of(email) : Optional.of("system");
        }
    }
}
