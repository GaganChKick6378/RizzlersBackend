package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.UrlShortenRequest;
import com.kdu.rizzlers.dto.out.UrlShortenResponse;
import com.kdu.rizzlers.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for URL shortening operations.
 */
@RestController
@RequestMapping("/url")
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    /**
     * Endpoint to shorten a URL.
     *
     * @param request The request containing the URL to shorten
     * @return The shortened URL
     */
    @PostMapping("/shorten")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@Valid @RequestBody UrlShortenRequest request) {
        log.info("Received request to shorten URL: {}", request.getUrl());
        
        String shortenedUrl = urlShortenerService.shortenUrl(request.getUrl());
        
        UrlShortenResponse response = UrlShortenResponse.builder()
                .originalUrl(request.getUrl())
                .shortUrl(shortenedUrl)
                .build();
                
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to handle validation errors.
     *
     * @return Error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error processing URL shortening request: {}", e.getMessage());
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to process request");
        
        if (e.getBindingResult().getFieldError() != null) {
            String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
            if (defaultMessage != null && defaultMessage.contains("Invalid URL format")) {
                errorResponse.put("message", "Invalid URL format");
            } else {
                errorResponse.put("message", defaultMessage);
            }
        } else {
            errorResponse.put("message", "Invalid request parameters");
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Endpoint to handle illegal argument exceptions (like invalid URL format).
     *
     * @return Error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument error in URL shortening request: {}", e.getMessage());
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to process request");
        
        if (e.getMessage().contains("Invalid URL format")) {
            errorResponse.put("message", "Invalid URL format");
        } else {
            errorResponse.put("message", e.getMessage());
        }
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Endpoint to handle general errors.
     *
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Error processing URL shortening request: {}", e.getMessage(), e);
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to process request");
        errorResponse.put("message", e.getMessage());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
} 