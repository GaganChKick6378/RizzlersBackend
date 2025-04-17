package com.kdu.rizzlers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * Creates a WebClient builder bean for making HTTP requests
     * 
     * @return WebClient.Builder instance
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    
    /**
     * Creates an ObjectMapper bean for JSON serialization/deserialization
     * 
     * @return ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public FilterRegistrationBean<AWSXRayServletFilter> filterRegistrationBean() {
        FilterRegistrationBean<AWSXRayServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AWSXRayServletFilter("Rizzlers-Backend"));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
} 