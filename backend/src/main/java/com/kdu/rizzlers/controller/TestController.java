package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple controller for testing database connectivity
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    @Autowired
    public StudentRepository studentRepository;

    /**
     * Test endpoint to verify database connectivity
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            long studentCount = studentRepository.count();
            response.put("success", true);
            response.put("message", "Database connection successful");
            response.put("studentCount", studentCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Database connection failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 