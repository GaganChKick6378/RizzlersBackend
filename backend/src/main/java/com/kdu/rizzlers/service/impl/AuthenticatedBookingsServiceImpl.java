package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.GuestBookingDTO;
import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.repository.ReviewRepository;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.service.AuthenticatedBookingsService;
import com.kdu.rizzlers.service.CognitoAuthService;
import com.kdu.rizzlers.service.GuestBookingsGraphQLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the AuthenticatedBookingsService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedBookingsServiceImpl implements AuthenticatedBookingsService {

    private final UserRepository userRepository;
    private final CognitoAuthService cognitoAuthService;
    private final GuestBookingsGraphQLService graphQLService;
    private final ReviewRepository reviewRepository;
    
    @Override
    @Transactional(readOnly = true)
    public GuestBookingsResponseDTO getBookingsForAuthenticatedUser(String idToken) {
        // Validate Cognito token
        String userEmail = cognitoAuthService.validateIdToken(idToken);
        
        if (userEmail == null) {
            log.warn("Authentication failed: invalid or expired token");
            return GuestBookingsResponseDTO.error("Authentication failed: invalid or expired token");
        }
        
        log.info("Authenticated user email: {}", userEmail);
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            log.warn("User not found for email: {}", userEmail);
            return GuestBookingsResponseDTO.error("User not found");
        }
        
        User user = userOpt.get();
        
        // Get guest ID from user record
        Integer guestId = user.getGuestId();
        if (guestId == null) {
            log.warn("No guest ID associated with authenticated user: {}", userEmail);
            return GuestBookingsResponseDTO.error("No guest ID found for this user");
        }
        
        log.info("Fetching bookings for authenticated user with guestId: {}", guestId);
        
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
            log.info("No bookings found for authenticated user with guestId: {}", guestId);
            return GuestBookingsResponseDTO.builder()
                    .success(true)
                    .message("No bookings found")
                    .bookings(Collections.emptyList())
                    .build();
        }
        
        // Return bookings
        return GuestBookingsResponseDTO.success(bookings);
    }
} 