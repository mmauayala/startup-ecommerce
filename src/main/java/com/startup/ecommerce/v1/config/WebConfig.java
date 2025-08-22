package com.startup.ecommerce.v1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final String[] allowedOrigins;

	public WebConfig(@Value("${app.cors.allowed-origins}") String allowedOriginsCsv) {
		this.allowedOrigins = allowedOriginsCsv.split("\\s*,\\s*");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping("/**")
			.allowedOrigins(allowedOrigins)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
			.allowCredentials(true)
			.maxAge(3600);
	}
}