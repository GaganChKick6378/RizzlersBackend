package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.TravelItineraryDto;

/**
 * Service for handling email operations
 */
public interface EmailService {
    
    /**
     * Sends a travel itinerary email with PDF attachment
     * 
     * @param itinerary The travel itinerary data
     * @param recipientEmail The recipient's email address
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendTravelItineraryEmail(TravelItineraryDto itinerary, String recipientEmail);
} 