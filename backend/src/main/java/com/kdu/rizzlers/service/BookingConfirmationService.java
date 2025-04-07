package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.BookingConfirmationRequest;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;

/**
 * Service interface for retrieving booking confirmation details
 */
public interface BookingConfirmationService {
    
    /**
     * Get detailed booking confirmation information
     * 
     * @param request The request containing booking ID and guest ID
     * @return Detailed booking confirmation information
     */
    BookingConfirmationDetailsResponse getBookingConfirmationDetails(BookingConfirmationRequest request);
} 