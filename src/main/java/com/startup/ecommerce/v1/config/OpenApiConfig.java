package com.startup.ecommerce.v1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("Startup Ecommerce API")
                    .version("1.0")
                    .description("API REST para la gestión de un e-commerce, incluyendo productos, órdenes, carrito y usuarios.")
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("""
                                Autenticación JWT usando el header Authorization.
                                
                                Para obtener un token:
                                1. Hacer POST a /api/auth/login con email y password
                                2. Usar el token recibido en el header: Bearer {token}
                                
                                Roles disponibles:
                                - ADMIN: Acceso total
                                - CLIENTE: Acceso limitado a sus propios recursos
                                """)));
    }
}
