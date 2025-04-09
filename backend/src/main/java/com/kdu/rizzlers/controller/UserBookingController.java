package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.entity.UserBooking;
import com.kdu.rizzlers.repository.UserBookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Bookings", description = "APIs for user booking management")
public class UserBookingController {

    private final UserBookingRepository userBookingRepository;
    
    /**
     * Get booking details by booking ID
     * 
     * @param bookingId the booking ID
     * @return the booking details
     */
    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get booking details by ID", description = "Retrieves booking details by booking ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking found",
                   content = @Content(schema = @Schema(implementation = UserBooking.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<UserBooking> getBookingDetailsByBookingId(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Integer bookingId) {
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
    @Operation(summary = "Get bookings by guest ID", description = "Retrieves all bookings for a guest")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                   content = @Content(schema = @Schema(implementation = UserBooking.class)))
    })
    public ResponseEntity<List<UserBooking>> getBookingsByGuestId(
            @Parameter(description = "Guest ID", required = true)
            @PathVariable Integer guestId) {
        log.info("Getting bookings for guest ID: {}", guestId);
        
        List<UserBooking> bookings = userBookingRepository.findByGuestId(guestId);
        
        return ResponseEntity.ok(bookings);
    }
} 