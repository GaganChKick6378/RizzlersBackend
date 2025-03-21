package com.kdu.rizzlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return getHealthResponse();
    }
    
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> apiHealthCheck() {
        return getHealthResponse();
    }
    
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> actuatorHealthCheck() {
        return getHealthResponse();
    }
    
    private ResponseEntity<Map<String, Object>> getHealthResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Rizzlers Backend API");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
} 