package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.entity.UserBooking;
import com.kdu.rizzlers.repository.UserBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for handling user booking operations
 */
@RestController
@RequestMapping("/user-bookings")
@RequiredArgsConstructor
@Slf4j
public class UserBookingController {

    private final UserBookingRepository userBookingRepository;
    
    /**
     * Get booking details by booking ID
     * 
     * @param bookingId the booking ID
     * @return the booking details
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<UserBooking> getBookingDetailsByBookingId(@PathVariable Integer bookingId) {
        log.info("Getting booking details for booking ID: {}", bookingId);
        
        Optional<UserBooking> bookingDetails = userBookingRepository.findByBookingId(bookingId);
        
        return bookingDetails
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all bookings for a guest
     * 
     * @param guestId the guest ID
     * @return list of bookings
     */
    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<UserBooking>> getBookingsByGuestId(@PathVariable Integer guestId) {
        log.info("Getting bookings for guest ID: {}", guestId);
        
        List<UserBooking> bookings = userBookingRepository.findByGuestId(guestId);
        
        return ResponseEntity.ok(bookings);
    }
} 