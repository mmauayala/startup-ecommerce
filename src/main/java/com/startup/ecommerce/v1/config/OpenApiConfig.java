package com.startup.ecommerce.v1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI ecommerceOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Tienda E-commerce API")
				.version("v1")
				.description("Backend API for modern apparel e-commerce"));
	}
}