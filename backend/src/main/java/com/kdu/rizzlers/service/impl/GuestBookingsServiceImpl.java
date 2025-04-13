package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.GuestBookingDTO;
import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpRequestDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpVerificationDTO;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.repository.ReviewRepository;
import com.kdu.rizzlers.service.EmailService;
import com.kdu.rizzlers.service.GuestBookingsGraphQLService;
import com.kdu.rizzlers.service.GuestBookingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of GuestBookingsService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuestBookingsServiceImpl implements GuestBookingsService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final GuestBookingsGraphQLService graphQLService;
    private final ReviewRepository reviewRepository;
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 10;
    
    @Override
    @Transactional
    public GuestBookingsResponseDTO requestOtp(MyBookingsOtpRequestDTO request) {
        String email = request.getEmail();
        
        log.info("Processing OTP request for email: {}", email);
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("User not found for email: {}.", email);
            return GuestBookingsResponseDTO.error("No user found with this email address. Please contact support.");
        }
        
        User user = userOpt.get();
        
        // Check if user has a guest ID
        if (user.getGuestId() == null) {
            log.warn("User has no associated guest ID: {}", email);
            return GuestBookingsResponseDTO.error("No guest ID associated with this email. Please contact support.");
        }
        
        // Generate and save OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(ZonedDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);
        
        // Default property name
        String propertyName = "Team 10 Hotel";
        
        // Send OTP email
        try {
            // Get guest name from user record
            String guestName = (user.getFirstName() != null) ? 
                    user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "") : 
                    "Guest";
                    
            emailService.sendMyBookingsOtpEmail(email, otp, guestName, propertyName);
            log.info("OTP sent to email: {}", email);
            return GuestBookingsResponseDTO.otpSent();
        } catch (Exception e) {
            log.error("Error sending OTP email", e);
            return GuestBookingsResponseDTO.error("Failed to send OTP email. Please try again.");
        }
    }
    
    @Override
    @Transactional
    public GuestBookingsResponseDTO verifyOtpAndGetBookings(MyBookingsOtpVerificationDTO request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        
        log.info("Verifying OTP for email: {}", email);
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("User not found for email: {}", email);
            return GuestBookingsResponseDTO.error("User not found");
        }
        
        User user = userOpt.get();
        
        // Verify OTP
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            log.warn("Invalid OTP provided for email: {}", email);
            return GuestBookingsResponseDTO.error("Invalid OTP");
        }
        
        // Check if OTP is expired
        if (user.getOtpExpiry() == null || ZonedDateTime.now().isAfter(user.getOtpExpiry())) {
            log.warn("OTP expired for email: {}", email);
            return GuestBookingsResponseDTO.error("OTP expired");
        }
        
        // Clear OTP after verification
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        
        // Get guest ID from user record
        Integer guestId = user.getGuestId();
        if (guestId == null) {
            log.warn("User has no associated guest ID: {}", email);
            return GuestBookingsResponseDTO.error("No guest ID found for this user");
        }
        
        log.info("Successfully verified OTP for email: {}. Fetching bookings for guestId: {}", email, guestId);
        
        // Fetch bookings from GraphQL using the guest ID from user record
        List<GuestBookingDTO> bookings = graphQLService.fetchGuestBookings(guestId);
        
        // Add room images to each booking
        for (GuestBookingDTO booking : bookings) {
            if (booking.getRoomTypeId() != null) {
                try {
                    String roomImage = reviewRepository.findFirstImageForRoomType(booking.getRoomTypeId());
                    booking.setRoomImage(roomImage);
                    log.info("Added room image for booking ID: {}, room type ID: {}", 
                            booking.getBookingId(), booking.getRoomTypeId());
                } catch (Exception e) {
                    log.warn("Error fetching room image for room type ID {}: {}", 
                            booking.getRoomTypeId(), e.getMessage());
                }
            }
        }
        
        if (bookings.isEmpty()) {
            log.info("No bookings found for guestId: {}", guestId);
            return GuestBookingsResponseDTO.builder()
                    .success(true)
                    .message("No bookings found")
                    .bookings(Collections.emptyList())
                    .build();
        }
        
        // Return bookings
        return GuestBookingsResponseDTO.success(bookings);
    }
    
    /**
     * Generate a 6-digit OTP
     * 
     * @return OTP as string
     */
    private String generateOtp() {
        // Generate 6-digit OTP
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
} 