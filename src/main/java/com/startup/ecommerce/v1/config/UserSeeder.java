package com.startup.ecommerce.v1.config;

import com.startup.ecommerce.v1.entities.UserEntity;
import com.startup.ecommerce.v1.entities.enums.Role;
import com.startup.ecommerce.v1.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class UserSeeder {
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner seedUsers() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (userRepository.findByEmail("admin@tienda.com").isEmpty()) {
                userRepository.save(UserEntity.builder()
                        .name("Administrador")
                        .email("admin@tienda.com")
                        .password(encoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build());
            }
            for (int i = 1; i <= 10; i++) {
                String email = "cliente" + i + "@mail.com";
                if (userRepository.findByEmail(email).isEmpty()) {
                    userRepository.save(UserEntity.builder()
                            .name("Cliente " + i)
                            .email(email)
                            .password(encoder.encode("cliente123"))
                            .role(Role.CLIENTE)
                            .enabled(true)
                            .build());
                }
            }
        };
    }
}
