package com.kdu.rizzlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping({"/", "/health", "/api/health", "/actuator/health", "/api", "/ping"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
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