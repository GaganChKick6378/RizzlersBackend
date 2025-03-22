package com.kdu.rizzlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping({"/", "/health", "/api/health", "/actuator/health", "/api", "/ping"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return getHealthResponse();
    }
    
    /**
     * Dedicated endpoint specifically for ELB health checks
     * This ensures we have a reliable endpoint for AWS load balancer health checks
     */
    @RequestMapping(path = "/api/health", produces = "application/json")
    public ResponseEntity<Map<String, Object>> elbHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Rizzlers Backend API");
        response.put("timestamp", System.currentTimeMillis());
        
        // Return a 200 OK response for the ELB health check
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<Map<String, Object>> getHealthResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Rizzlers Backend API");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
} 