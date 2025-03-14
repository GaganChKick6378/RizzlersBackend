package com.kdu.rizzlers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web configuration for the application.
 * Provides CORS configuration without Spring Security.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${application.cors.allowed-origins:http://localhost:3000,https://dev.rizzlers.kdu.ac.in}")
    private String allowedOrigins;
    
    @Value("${application.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;
    
    @Value("${application.cors.allowed-headers:Content-Type,Authorization,X-Requested-With}")
    private String allowedHeaders;
    
    /**
     * Configure CORS settings.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(methods);
        config.setAllowedHeaders(headers);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 