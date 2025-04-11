package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.TravelItineraryDto;
import com.kdu.rizzlers.dto.BookingDetailsDTO;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;

/**
 * Service for handling email operations
 */
public interface EmailService {
    
    /**
     * Sends a travel itinerary email with PDF attachment using the legacy DTO
     * 
     * @param itinerary The travel itinerary data
     * @param recipientEmail The recipient's email address
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendTravelItineraryEmail(TravelItineraryDto itinerary, String recipientEmail);
    
    /**
     * Sends a travel itinerary email with PDF attachment using booking confirmation details
     * 
     * @param bookingDetails The booking confirmation details
     * @param recipientEmail The recipient's email address
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendTravelItineraryEmail(BookingConfirmationDetailsResponse bookingDetails, String recipientEmail);

    /**
     * Sends an OTP email for booking cancellation
     * 
     * @param to The recipient's email address
     * @param otp The OTP code
     * @param bookingDetails The booking details
     */
    void sendOtpEmail(String to, String otp, BookingDetailsDTO bookingDetails);
    
    /**
     * Sends an OTP email for accessing "My Bookings"
     * 
     * @param to The recipient's email address
     * @param otp The OTP code
     * @param guestName The guest's name
     * @param propertyName The property name
     */
    void sendMyBookingsOtpEmail(String to, String otp, String guestName, String propertyName);
    
    /**
     * Sends a review invitation email to a guest after checkout
     * 
     * @param to The recipient's email address
     * @param reviewLink The link to the review form with token
     * @param guestName The guest's name
     * @param propertyName The property name
     * @param expiryDays Number of days until the review link expires
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendReviewInvitationEmail(String to, String reviewLink, String guestName, String propertyName, int expiryDays);
} 