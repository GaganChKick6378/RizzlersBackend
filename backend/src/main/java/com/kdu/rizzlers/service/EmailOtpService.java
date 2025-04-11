package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.OtpResponseDTO;

/**
 * Service for handling email OTP operations
 */
public interface EmailOtpService {
    
    /**
     * Generate and send an OTP to the specified email
     * 
     * @param email the recipient's email address
     * @return OtpResponseDTO containing status and message
     */
    OtpResponseDTO generateAndSendOtp(String email);
    
    /**
     * Verify the OTP provided by the user
     * 
     * @param email the email address the OTP was sent to
     * @param otp the OTP code provided by the user
     * @return OtpResponseDTO containing status, message, and session information if verified
     */
    OtpResponseDTO verifyOtp(String email, String otp);
    
    /**
     * Resend OTP to the user's email
     * 
     * @param email the recipient's email address
     * @return OtpResponseDTO containing status and message
     */
    OtpResponseDTO resendOtp(String email);
    
    /**
     * Clean up expired OTPs
     * 
     * @return number of records cleaned up
     */
    int cleanupExpiredOtps();
} 