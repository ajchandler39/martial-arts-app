package com.alijah.martial_arts_app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class MartialArtsBackendApplication {

	@Value("${cors.allowed-origin}")
	private String corsAllowedOrigin;

	public static void main(String[] args) {
		SpringApplication.run(MartialArtsBackendApplication.class, args);
	}

	// Allows the front-end application to send CRUD operations to the back-end.
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
						.allowedOrigins(corsAllowedOrigin)
						.allowedMethods("GET", "POST", "PUT", "DELETE");
			}
		};
	}
}