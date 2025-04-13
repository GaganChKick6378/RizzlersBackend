package com.kdu.rizzlers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
        
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection
            .cors(AbstractHttpConfigurer::disable) // Disable CORS protection
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Don't create sessions
            .formLogin(AbstractHttpConfigurer::disable) // Disable form login
            .httpBasic(configurer -> {}) // Enable HTTP Basic for API endpoints
            .authorizeHttpRequests(auth -> auth
                // Explicitly permit health check endpoints with highest priority
                .requestMatchers(
                    mvcMatcherBuilder.pattern("/"),
                    mvcMatcherBuilder.pattern("/ping"),
                    mvcMatcherBuilder.pattern("/health"),
                    mvcMatcherBuilder.pattern("/healthcheck"),
                    mvcMatcherBuilder.pattern("/api/health"),
                    mvcMatcherBuilder.pattern("/actuator/health"),
                    mvcMatcherBuilder.pattern("/swagger-ui/**"),
                    mvcMatcherBuilder.pattern("/v3/api-docs/**")
                ).permitAll()
                
                // Housekeeping endpoints with role-based access
                .requestMatchers(mvcMatcherBuilder.pattern("/api/housekeeping/admin/**")).hasRole("ADMIN")
                .requestMatchers(mvcMatcherBuilder.pattern("/api/housekeeping/staff/**")).hasAnyRole("ADMIN", "STAFF")
                
                // Allow all other requests without authentication
                .requestMatchers(mvcMatcherBuilder.pattern("/**")).permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 