package com.startup.ecommerce.v1.controllers;

import com.startup.ecommerce.v1.dto.AuthRequestDto;
import com.startup.ecommerce.v1.dto.AuthResponseDto;
import com.startup.ecommerce.v1.dto.RegisterRequestDto;
import com.startup.ecommerce.v1.entities.UserEntity;
import com.startup.ecommerce.v1.entities.enums.Role;
import com.startup.ecommerce.v1.repositories.UserRepository;
import com.startup.ecommerce.v1.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y gestión de usuarios")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un JWT.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
        })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDto request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponseDto(token, user.getEmail(), user.getRole().name()));
    }

    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario cliente y devuelve un JWT.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Registro exitoso", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Email ya registrado")
        })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }
        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENTE)
                .enabled(true)
                .build();
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponseDto(token, user.getEmail(), user.getRole().name()));
    }

    @Operation(summary = "Renovar token JWT", description = "Devuelve un nuevo JWT si el actual es válido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token renovado", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado")
        })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token no proporcionado");
        }
        String oldToken = authHeader.substring(7);
        if (!jwtUtil.validateToken(oldToken)) {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }
        String email = jwtUtil.extractUsername(oldToken);
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponseDto(newToken, user.getEmail(), user.getRole().name()));
    }

    @Operation(summary = "Logout", description = "Logout del usuario (elimina el token del lado del cliente)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Logout exitoso")
        })
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // En JWT, el logout es responsabilidad del frontend (borrar token del almacenamiento)
        return ResponseEntity.ok("Logout exitoso (el token debe ser eliminado del lado del cliente)");
    }
}
