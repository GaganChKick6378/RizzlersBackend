package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpRequestDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpVerificationDTO;

/**
 * Service for handling guest bookings operations
 */
public interface GuestBookingsService {
    
    /**
     * Request OTP for accessing "My Bookings"
     * 
     * @param request DTO containing the email
     * @return ResponseDTO with status of the OTP request
     */
    GuestBookingsResponseDTO requestOtp(MyBookingsOtpRequestDTO request);
    
    /**
     * Verify OTP and retrieve guest bookings if valid
     * 
     * @param request DTO containing the email and OTP
     * @return ResponseDTO with list of bookings or error message
     */
    GuestBookingsResponseDTO verifyOtpAndGetBookings(MyBookingsOtpVerificationDTO request);
} 