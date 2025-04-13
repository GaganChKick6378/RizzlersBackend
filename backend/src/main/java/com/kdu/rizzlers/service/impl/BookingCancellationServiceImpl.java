package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.BookingCancellationResponse;
import com.kdu.rizzlers.dto.BookingDetailsDTO;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.service.BookingCancellationService;
import com.kdu.rizzlers.service.CognitoAuthService;
import com.kdu.rizzlers.service.EmailService;
import com.kdu.rizzlers.service.GraphQLService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCancellationServiceImpl implements BookingCancellationService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final GraphQLService graphQLService;
    private final CognitoAuthService cognitoAuthService;
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 10;
    
    @Override
    @Transactional
    public BookingCancellationResponse requestCancellation(Integer guestId, Integer bookingId) {
        log.info("Processing cancellation request for booking: {}, guest: {}", bookingId, guestId);
        
        // Find user by guest ID
        Optional<User> userOpt = userRepository.findByGuestId(guestId);
        if (userOpt.isEmpty()) {
            return BookingCancellationResponse.error("User not found for the provided guest ID");
        }
        
        User user = userOpt.get();
        
        // Check if booking exists and get its status
        Map<String, Object> bookingStatus = checkBookingStatus(bookingId);
        if (bookingStatus == null) {
            return BookingCancellationResponse.error("Booking not found");
        }
        
        // Check if booking is already cancelled
        Integer statusId = (Integer) bookingStatus.get("status_id");
        String status = (String) bookingStatus.get("status");
        
        if (statusId == 2) {
            log.info("Booking {} is already cancelled (status_id=2, status={}). Cancellation request rejected.", bookingId, status);
            return BookingCancellationResponse.builder()
                    .success(false)
                    .message("Booking is already cancelled")
                    .bookingId(bookingId)
                    .statusId(statusId)
                    .guestId(guestId)
                    .status(status)
                    .build();
        }
        
        // Fetch booking details using GraphQL
        BookingDetailsDTO bookingDetails;
        try {
            bookingDetails = fetchBookingDetails(bookingId);
            if (bookingDetails == null) {
                return BookingCancellationResponse.error("Unable to fetch booking details");
            }
        } catch (Exception e) {
            log.error("Error fetching booking details", e);
            return BookingCancellationResponse.error("Unable to fetch booking details");
        }
        
        // Generate and save OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(ZonedDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);
        
        // Send OTP email
        try {
            // Set guest details in the DTO
            bookingDetails.setGuestId(guestId);
            bookingDetails.setGuestName(user.getFirstName() + " " + user.getLastName());
            bookingDetails.setGuestEmail(user.getEmail());
            
            emailService.sendOtpEmail(user.getEmail(), otp, bookingDetails);
            log.info("OTP sent to user email: {}", user.getEmail());
            return BookingCancellationResponse.otpSent();
        } catch (Exception e) {
            log.error("Error sending OTP email", e);
            return BookingCancellationResponse.error("Failed to send OTP email");
        }
    }
    
    @Override
    @Transactional
    public BookingCancellationResponse verifyCancellation(Integer guestId, Integer bookingId, String otp) {
        log.info("Verifying OTP for booking cancellation: {}, guest: {}", bookingId, guestId);
        
        // Find user by guest ID
        Optional<User> userOpt = userRepository.findByGuestId(guestId);
        if (userOpt.isEmpty()) {
            return BookingCancellationResponse.error("User not found for the provided guest ID");
        }
        
        User user = userOpt.get();
        
        // Verify OTP
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            return BookingCancellationResponse.error("Invalid OTP");
        }
        
        // Check if OTP is expired
        if (user.getOtpExpiry() == null || ZonedDateTime.now().isAfter(user.getOtpExpiry())) {
            return BookingCancellationResponse.error("OTP expired");
        }
        
        // Clear OTP after verification
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        
        // Check if booking is already cancelled
        Map<String, Object> bookingStatus = checkBookingStatus(bookingId);
        if (bookingStatus != null) {
            Integer statusId = (Integer) bookingStatus.get("status_id");
            String status = (String) bookingStatus.get("status");
            
            if (statusId == 2) {
                log.info("Booking {} is already cancelled (status_id=2, status={}). Cancellation request rejected.", bookingId, status);
                return BookingCancellationResponse.builder()
                        .success(false)
                        .message("Booking is already cancelled")
                        .bookingId(bookingId)
                        .statusId(statusId)
                        .guestId(guestId)
                        .status(status)
                        .build();
            }
        } else {
            return BookingCancellationResponse.error("Booking not found or unable to check booking status");
        }
        
        // Cancel booking using GraphQL mutation
        try {
            Map<String, Object> response = cancelBookingInGraphQL(bookingId);
            if (response != null && response.containsKey("updateBooking")) {
                Map<String, Object> updateBooking = (Map<String, Object>) response.get("updateBooking");
                
                // Create response with booking details
                return BookingCancellationResponse.builder()
                        .success(true)
                        .message("Booking cancelled successfully")
                        .bookingId((Integer) updateBooking.get("booking_id"))
                        .statusId((Integer) updateBooking.get("status_id"))
                        .propertyId((Integer) updateBooking.get("property_id"))
                        .guestId((Integer) updateBooking.get("guest_id"))
                        .status(getStatusFromResponse(updateBooking))
                        .build();
            }
            
            return BookingCancellationResponse.error("Failed to cancel booking");
        } catch (Exception e) {
            log.error("Error cancelling booking in GraphQL", e);
            return BookingCancellationResponse.error("Error cancelling booking: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BookingCancellationResponse authenticatedCancellation(Integer guestId, Integer bookingId, String idToken) {
        log.info("Processing authenticated cancellation request for booking: {}, guest: {}", bookingId, guestId);
        
        // Validate Cognito token
        if (!cognitoAuthService.isAuthenticated(idToken)) {
            log.warn("Authentication failed for ID token when attempting to cancel booking: {}", bookingId);
            return BookingCancellationResponse.error("Authentication failed");
        }
        
        String userEmail = cognitoAuthService.validateIdToken(idToken);
        log.info("Authenticated user with email: {} is cancelling booking: {}", userEmail, bookingId);
        
        // Find user by guest ID
        Optional<User> userOpt = userRepository.findByGuestId(guestId);
        if (userOpt.isEmpty()) {
            return BookingCancellationResponse.error("User not found for the provided guest ID");
        }
        
        User user = userOpt.get();
        
        // Optional validation: Check if the authenticated user's email matches the user's email
        if (!user.getEmail().equalsIgnoreCase(userEmail)) {
            log.warn("Email mismatch for authenticated user. Token email: {}, User email: {}", 
                     userEmail, user.getEmail());
            return BookingCancellationResponse.error("Authenticated user does not match booking owner");
        }
        
        // Check if booking is already cancelled
        Map<String, Object> bookingStatus = checkBookingStatus(bookingId);
        if (bookingStatus == null) {
            return BookingCancellationResponse.error("Booking not found");
        }
        
        Integer statusId = (Integer) bookingStatus.get("status_id");
        String status = (String) bookingStatus.get("status");
        
        if (statusId == 2) {
            log.info("Booking {} is already cancelled (status_id=2, status={}). Cancellation request rejected.", 
                     bookingId, status);
            return BookingCancellationResponse.builder()
                    .success(false)
                    .message("Booking is already cancelled")
                    .bookingId(bookingId)
                    .statusId(statusId)
                    .guestId(guestId)
                    .status(status)
                    .build();
        }
        
        // Cancel booking directly using GraphQL
        try {
            Map<String, Object> response = cancelBookingInGraphQL(bookingId);
            if (response != null && response.containsKey("updateBooking")) {
                Map<String, Object> updateBooking = (Map<String, Object>) response.get("updateBooking");
                
                // Create response with booking details
                return BookingCancellationResponse.builder()
                        .success(true)
                        .message("Booking cancelled successfully")
                        .bookingId((Integer) updateBooking.get("booking_id"))
                        .statusId((Integer) updateBooking.get("status_id"))
                        .propertyId((Integer) updateBooking.get("property_id"))
                        .guestId((Integer) updateBooking.get("guest_id"))
                        .status(getStatusFromResponse(updateBooking))
                        .build();
            }
            
            return BookingCancellationResponse.error("Failed to cancel booking");
        } catch (Exception e) {
            log.error("Error cancelling booking in GraphQL", e);
            return BookingCancellationResponse.error("Error cancelling booking: " + e.getMessage());
        }
    }
    
    /**
     * Check the current status of a booking
     * 
     * @param bookingId The booking ID to check
     * @return Map containing status_id and status or null if booking not found or error occurs
     */
    private Map<String, Object> checkBookingStatus(Integer bookingId) {
        String query = "query {\n" +
                "  getBooking(where: {booking_id: " + bookingId + "}) {\n" +
                "    booking_status {\n" +
                "      status\n" +
                "      status_id\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        try {
            Map<String, Object> response = graphQLService.executeQuery(query);
            
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> booking = (Map<String, Object>) data.get("getBooking");
                
                if (booking == null) {
                    log.warn("Booking with ID {} not found", bookingId);
                    return null;
                }
                
                if (booking.containsKey("booking_status")) {
                    return (Map<String, Object>) booking.get("booking_status");
                } else {
                    log.warn("Booking status not found for booking ID {}", bookingId);
                    return null;
                }
            } else {
                log.warn("Invalid response from GraphQL for booking ID {}", bookingId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error checking booking status for booking ID {}: {}", bookingId, e.getMessage(), e);
            return null;
        }
    }
    
    private String getStatusFromResponse(Map<String, Object> updateBooking) {
        try {
            Map<String, Object> bookingStatus = (Map<String, Object>) updateBooking.get("booking_status");
            return bookingStatus != null ? (String) bookingStatus.get("status") : null;
        } catch (Exception e) {
            log.error("Error extracting status from response", e);
            return null;
        }
    }
    
    private String generateOtp() {
        // Generate 6-digit OTP
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
    
    private BookingDetailsDTO fetchBookingDetails(Integer bookingId) {
        // GraphQL query to fetch booking details
        String query = "query {\n" +
                "  getBooking(where: {booking_id: " + bookingId + "}) {\n" +
                "    property_id\n" +
                "    check_in_date\n" +
                "    check_out_date\n" +
                "    booking_id\n" +
                "    room_booked {\n" +
                "      property {\n" +
                "        property_name\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        Map<String, Object> response = graphQLService.executeQuery(query);
        
        try {
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> booking = (Map<String, Object>) data.get("getBooking");
                
                if (booking != null) {
                    // Extract property name from the first room (all rooms should be from same property)
                    String propertyName = null;
                    if (booking.containsKey("room_booked")) {
                        java.util.List<Map<String, Object>> rooms = (java.util.List<Map<String, Object>>) booking.get("room_booked");
                        if (!rooms.isEmpty()) {
                            Map<String, Object> room = rooms.get(0);
                            Map<String, Object> property = (Map<String, Object>) room.get("property");
                            propertyName = (String) property.get("property_name");
                        }
                    }
                    
                    // Parse dates from strings
                    ZonedDateTime checkInDate = ZonedDateTime.parse((String) booking.get("check_in_date"));
                    ZonedDateTime checkOutDate = ZonedDateTime.parse((String) booking.get("check_out_date"));
                    
                    return BookingDetailsDTO.builder()
                            .bookingId((Integer) booking.get("booking_id"))
                            .propertyId((Integer) booking.get("property_id"))
                            .propertyName(propertyName)
                            .checkInDate(checkInDate)
                            .checkOutDate(checkOutDate)
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error parsing booking details from GraphQL", e);
        }
        
        return null;
    }
    
    private Map<String, Object> cancelBookingInGraphQL(Integer bookingId) {
        // GraphQL mutation to update booking status to cancelled (status_id = 2)
        String mutation = "mutation {\n" +
                "  updateBooking(\n" +
                "    where: {booking_id: " + bookingId + "}\n" +
                "    data: {booking_status: {connect: {status_id: 2}}}\n" +
                "  ) {\n" +
                "    booking_id\n" +
                "    status_id\n" +
                "    property_id\n" +
                "    guest_id\n" +
                "    booking_status {\n" +
                "      status\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        Map<String, Object> response = graphQLService.executeQuery(mutation);
        
        if (response != null && response.containsKey("data")) {
            return (Map<String, Object>) response.get("data");
        }
        
        return null;
    }
} 