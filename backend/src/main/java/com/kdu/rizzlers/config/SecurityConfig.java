package com.kdu.rizzlers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection
            .cors(AbstractHttpConfigurer::disable) // Disable CORS protection
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Don't create sessions
            .formLogin(AbstractHttpConfigurer::disable) // Disable form login
            .httpBasic(AbstractHttpConfigurer::disable) // Disable HTTP Basic
            .authorizeHttpRequests(auth -> auth
                // Explicitly permit health check endpoints with highest priority
                .requestMatchers("/", "/ping", "/health", "/healthcheck", "/api/health", "/actuator/health").permitAll()
                // Allow all other requests without authentication
                .requestMatchers("/**").permitAll()
            );
        
        return http.build();
    }
} 