package com.kdu.rizzlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/diagnostic")
public class DiagnosticController {

    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Add request info
        response.put("remoteAddr", request.getRemoteAddr());
        response.put("method", request.getMethod());
        response.put("requestURI", request.getRequestURI());
        response.put("serverName", request.getServerName());
        response.put("serverPort", request.getServerPort());
        
        // Add headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        response.put("headers", headers);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> version() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", "1.0.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
} 