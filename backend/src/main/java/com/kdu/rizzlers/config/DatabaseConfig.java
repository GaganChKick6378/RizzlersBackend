package com.kdu.rizzlers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class DatabaseConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("spring.datasource.driver-class-name"));
        dataSource.setUrl(env.getRequiredProperty("spring.datasource.url"));
        dataSource.setUsername(env.getRequiredProperty("spring.datasource.username"));
        dataSource.setPassword(env.getRequiredProperty("spring.datasource.password"));
        return dataSource;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        String allowedOrigins = env.getProperty("application.cors.allowed-origins");
        if (allowedOrigins != null) {
            config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        }
        
        String allowedMethods = env.getProperty("application.cors.allowed-methods");
        if (allowedMethods != null) {
            config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        }
        
        String allowedHeaders = env.getProperty("application.cors.allowed-headers");
        if (allowedHeaders != null) {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        
        config.setAllowCredentials(true);
        config.setMaxAge(env.getProperty("application.cors.max-age", Long.class, 3600L));
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 