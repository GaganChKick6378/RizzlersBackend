package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.OtpResponseDTO;
import com.kdu.rizzlers.entity.EmailOtp;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.repository.EmailOtpRepository;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.service.EmailOtpService;
import com.kdu.rizzlers.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOtpServiceImpl implements EmailOtpService {

    private final EmailOtpRepository emailOtpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    @Value("${app.otp.length}")
    private int otpLength;
    
    @Value("${app.otp.expiry.minutes}")
    private int otpExpiryMinutes;
    
    @Value("${app.otp.max-attempts}")
    private int maxAttempts;
    
    @Value("${app.session.expiry.hours}")
    private int sessionExpiryHours;
    
    /**
     * Generate a random numeric OTP of specified length
     * @return the generated OTP
     */
    private String generateOtp() {
        // Use SecureRandom for better security
        SecureRandom secureRandom = new SecureRandom();
        
        // Ensure OTP is always 6 digits (or specified length)
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        
        // Generate a number between min and max (inclusive)
        int randomNum = min + secureRandom.nextInt(max - min + 1);
        
        return String.valueOf(randomNum);
    }
    
    @Override
    @Transactional
    public OtpResponseDTO generateAndSendOtp(String email) {
        if (email == null || email.isBlank()) {
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("Email is required")
                    .build();
        }
        
        // Delete any existing OTPs for this email
        emailOtpRepository.deleteByEmail(email);
        
        String otp = generateOtp();
        ZonedDateTime expiryTime = ZonedDateTime.now().plusMinutes(otpExpiryMinutes);
        
        // Create new OTP record
        EmailOtp emailOtp = EmailOtp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(expiryTime)
                .verified(false)
                .attemptCount(0)
                .build();
        
        // Save to database first
        emailOtp = emailOtpRepository.save(emailOtp);
        
        // Get the actual saved OTP from the database entity to ensure consistency
        String savedOtp = emailOtp.getOtp();
        
        // Send OTP email with the saved OTP from the database
        try {
            // Using a simple email without booking details for verification
            emailService.sendMyBookingsOtpEmail(email, savedOtp, "", "");
            
            return OtpResponseDTO.builder()
                    .success(true)
                    .message("OTP sent successfully")
                    .otpExpiryMinutes(otpExpiryMinutes)
                    .otpExpiryTime(expiryTime)
                    .remainingAttempts(maxAttempts)
                    .build();
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage(), e);
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("Failed to send OTP email. Please try again.")
                    .build();
        }
    }
    
    @Override
    @Transactional
    public OtpResponseDTO verifyOtp(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("Email and OTP are required")
                    .build();
        }
        
        // Find the latest OTP for this email
        Optional<EmailOtp> latestOtpOpt = emailOtpRepository.findLatestByEmail(email);
        
        if (latestOtpOpt.isEmpty()) {
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("No OTP found for this email. Please request a new OTP.")
                    .build();
        }
        
        EmailOtp emailOtp = latestOtpOpt.get();
        
        // Check if OTP is expired
        if (ZonedDateTime.now().isAfter(emailOtp.getExpiryTime())) {
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("OTP has expired. Please request a new OTP.")
                    .build();
        }
        
        // Update attempt count
        emailOtp.setAttemptCount(emailOtp.getAttemptCount() + 1);
        emailOtp.setLastAttemptTime(ZonedDateTime.now());
        
        // Check if max attempts reached
        if (emailOtp.getAttemptCount() > maxAttempts) {
            emailOtpRepository.save(emailOtp);
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("Maximum attempts reached. Please request a new OTP.")
                    .build();
        }
        
        // Verify OTP
        if (!emailOtp.getOtp().equals(otp)) {
            int remainingAttempts = maxAttempts - emailOtp.getAttemptCount();
            emailOtpRepository.save(emailOtp);
            
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("Invalid OTP. " + (remainingAttempts > 0 ? remainingAttempts + " attempts remaining." : "No attempts remaining."))
                    .remainingAttempts(Math.max(0, remainingAttempts))
                    .build();
        }
        
        // OTP is valid, mark as verified
        emailOtp.setVerified(true);
        
        // Set session expiry
        ZonedDateTime sessionExpiryTime = ZonedDateTime.now().plusHours(sessionExpiryHours);
        emailOtp.setSessionExpiryTime(sessionExpiryTime);
        
        emailOtpRepository.save(emailOtp);
        
        // Update user's email verified status if they exist
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            userRepository.save(user);
        }
        
        // Generate a session token
        String sessionToken = UUID.randomUUID().toString();
        
        return OtpResponseDTO.builder()
                .success(true)
                .message("OTP verified successfully")
                .sessionToken(sessionToken)
                .sessionExpiryTime(sessionExpiryTime)
                .build();
    }
    
    @Override
    @Transactional
    public OtpResponseDTO resendOtp(String email) {
        if (email == null || email.isBlank()) {
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("Email is required")
                    .build();
        }
        
        // Check if there is an existing OTP for this email
        Optional<EmailOtp> latestOtpOpt = emailOtpRepository.findLatestByEmail(email);
        
        if (latestOtpOpt.isEmpty()) {
            // No existing OTP, generate a new one
            return generateAndSendOtp(email);
        }
        
        EmailOtp existingOtp = latestOtpOpt.get();
        
        // Check if the existing OTP is verified
        if (existingOtp.getVerified()) {
            return OtpResponseDTO.builder()
                    .success(false)
                    .message("Email is already verified")
                    .build();
        }
        
        // Check if the existing OTP is still valid
        if (ZonedDateTime.now().isBefore(existingOtp.getExpiryTime())) {
            // OTP is still valid, resend the same OTP
            try {
                // Get the OTP directly from the database entity
                String savedOtp = existingOtp.getOtp();
                
                // Using a simple email without booking details for verification
                emailService.sendMyBookingsOtpEmail(email, savedOtp, "", "");
                
                return OtpResponseDTO.builder()
                        .success(true)
                        .message("OTP resent successfully")
                        .otpExpiryMinutes(otpExpiryMinutes)
                        .otpExpiryTime(existingOtp.getExpiryTime())
                        .remainingAttempts(maxAttempts - existingOtp.getAttemptCount())
                        .build();
            } catch (Exception e) {
                log.error("Failed to resend OTP email: {}", e.getMessage(), e);
                return OtpResponseDTO.builder()
                        .success(false)
                        .message("Failed to resend OTP email. Please try again.")
                        .build();
            }
        } else {
            // OTP has expired, generate a new one
            return generateAndSendOtp(email);
        }
    }
    
    @Override
    @Transactional
    public int cleanupExpiredOtps() {
        ZonedDateTime now = ZonedDateTime.now();
        log.info("Starting expired OTP cleanup task at {}", now);
        
        try {
            int deletedCount = emailOtpRepository.clearExpiredOtps(now);
            log.info("Successfully cleaned up {} expired OTPs", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("Error cleaning up expired OTPs: {}", e.getMessage(), e);
            return 0;
        }
    }
} 