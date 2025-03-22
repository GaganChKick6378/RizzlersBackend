package com.kdu.rizzlers.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuration for health checks that ensures the application
 * responds to health check requests regardless of context path
 */
@Configuration
public class HealthCheckConfig {

    /**
     * Custom health indicator that ensures the application is UP
     */
    @Bean
    public HealthIndicator customHealthIndicator() {
        return () -> Health.up()
                .withDetail("status", "UP")
                .withDetail("service", "Rizzlers Backend API")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
    }

    /**
     * Filter to intercept health check requests at the root level
     * This ensures health checks work even with context path configured
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> healthCheckFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                            HttpServletResponse response, 
                                            FilterChain filterChain) throws ServletException, IOException {
                
                String path = request.getRequestURI();
                
                // Intercept health check paths at root level
                if (path.equals("/") || 
                    path.equals("/ping") || 
                    path.equals("/health") || 
                    path.equals("/healthcheck")) {
                    
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"status\":\"UP\",\"service\":\"Rizzlers Backend API\",\"timestamp\":" + System.currentTimeMillis() + "}");
                    return;
                }
                
                filterChain.doFilter(request, response);
            }
        });
        
        registration.addUrlPatterns("/", "/ping", "/health", "/healthcheck");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
    
    /**
     * Dedicated filter specifically for the ELB health check path at /api/health
     * This ensures the ELB health check always gets a quick and reliable response
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> elbHealthCheckFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                            HttpServletResponse response, 
                                            FilterChain filterChain) throws ServletException, IOException {
                
                // Immediate response for ELB health checks, bypassing all other filters
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"status\":\"UP\",\"service\":\"Rizzlers Backend API\",\"timestamp\":" + System.currentTimeMillis() + "}");
            }
        });
        
        // Specifically target the ELB health check path
        registration.addUrlPatterns("/api/health");
        // Set to highest priority to ensure it runs before any other filters
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
} 