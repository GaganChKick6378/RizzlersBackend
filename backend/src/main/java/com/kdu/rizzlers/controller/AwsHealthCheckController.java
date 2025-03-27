package com.kdu.rizzlers.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dedicated controller specifically for AWS ELB health checks
 * This controller is designed to be extremely lightweight and fast
 */
@RestController
@RequestMapping("/health")
public class AwsHealthCheckController {

    /**
     * Simple health check endpoint for AWS ELB
     * This method is specifically designed to respond to AWS health checks
     * with minimal overhead and processing
     * 
     * @return A simple 200 OK response
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> healthCheck() {
        // Simple, fast response with no processing requirements
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }
} 