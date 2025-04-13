package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;

/**
 * Service for managing authenticated user bookings
 */
public interface AuthenticatedBookingsService {
    
    /**
     * Get bookings for authenticated user using Cognito ID token
     * 
     * @param idToken The Cognito ID token
     * @return Response DTO with bookings list or error message
     */
    GuestBookingsResponseDTO getBookingsForAuthenticatedUser(String idToken);
} 