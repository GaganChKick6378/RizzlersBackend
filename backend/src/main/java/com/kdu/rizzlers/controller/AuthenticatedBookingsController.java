package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.service.AuthenticatedBookingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authenticated user bookings requests
 */
@RestController
@RequestMapping("/authenticated-bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authenticated Bookings", description = "APIs for managing authenticated user bookings")
public class AuthenticatedBookingsController {
    
    private final AuthenticatedBookingsService authenticatedBookingsService;
    
    /**
     * Get bookings for authenticated user
     * 
     * @param authorization ID token from Cognito in Authorization header
     * @return Response DTO with bookings list or error message
     */
    @GetMapping
    @Operation(summary = "Get authenticated user bookings", 
              description = "Retrieve bookings for authenticated user using Cognito token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GuestBookingsResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                    content = @Content(schema = @Schema(implementation = GuestBookingsResponseDTO.class)))
    })
    public ResponseEntity<GuestBookingsResponseDTO> getAuthenticatedUserBookings(
            @Parameter(description = "ID token from Cognito", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        // Extract token from Authorization header
        String idToken = extractToken(authorization);
        if (idToken == null) {
            log.warn("No valid token provided in Authorization header");
            return ResponseEntity.status(401).body(
                    GuestBookingsResponseDTO.error("Authentication required"));
        }
        
        log.info("Processing authenticated bookings request");
        
        GuestBookingsResponseDTO response = authenticatedBookingsService.getBookingsForAuthenticatedUser(idToken);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (response.getMessage() != null && 
                (response.getMessage().contains("Authentication failed") || 
                 response.getMessage().contains("Invalid token"))) {
            return ResponseEntity.status(401).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Extract token from Authorization header
     * 
     * @param authorizationHeader The Authorization header value
     * @return The token or null if not found/invalid
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return null;
        }
        
        // Check if it's a Bearer token
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        
        // If not formatted as Bearer token, return the value as is
        return authorizationHeader;
    }
} 