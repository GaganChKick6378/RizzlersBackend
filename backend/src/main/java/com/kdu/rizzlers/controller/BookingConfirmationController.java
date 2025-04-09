package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.BookingConfirmationRequest;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
import com.kdu.rizzlers.service.BookingConfirmationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for booking confirmation details operations
 */
@RestController
@RequestMapping("/booking-confirmation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Confirmation", description = "APIs for retrieving booking confirmation details")
public class BookingConfirmationController {

    private final BookingConfirmationService bookingConfirmationService;
    
    /**
     * Get detailed booking confirmation information
     * 
     * @param request The request containing booking ID and guest ID
     * @return Detailed booking confirmation information
     */
    @PostMapping("/details")
    @Operation(summary = "Get booking confirmation details", description = "Retrieves detailed booking confirmation information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking confirmation details retrieved successfully",
                     content = @Content(schema = @Schema(implementation = BookingConfirmationDetailsResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or booking not found",
                     content = @Content(schema = @Schema(implementation = BookingConfirmationDetailsResponse.class)))
    })
    public ResponseEntity<BookingConfirmationDetailsResponse> getBookingConfirmationDetails(
            @Parameter(description = "Booking confirmation request with booking ID and guest ID", required = true)
            @Valid @RequestBody BookingConfirmationRequest request) {
        
        log.info("Received request for booking confirmation details: bookingId={}, guestId={}",
                request.getBookingId(), request.getGuestId());
        
        BookingConfirmationDetailsResponse response = 
                bookingConfirmationService.getBookingConfirmationDetails(request);
        
        if (Boolean.FALSE.equals(response.getSuccess())) {
            log.warn("Failed to retrieve booking confirmation details: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get detailed booking confirmation information using path variables
     * 
     * @param bookingId The booking ID
     * @param guestId The guest ID
     * @return Detailed booking confirmation information
     */
    @GetMapping("/{bookingId}/{guestId}")
    @Operation(summary = "Get booking confirmation details by path", 
               description = "Retrieves detailed booking confirmation information using path variables")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking confirmation details retrieved successfully",
                     content = @Content(schema = @Schema(implementation = BookingConfirmationDetailsResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid parameters or booking not found",
                     content = @Content(schema = @Schema(implementation = BookingConfirmationDetailsResponse.class)))
    })
    public ResponseEntity<BookingConfirmationDetailsResponse> getBookingConfirmationDetailsByPath(
            @Parameter(description = "Booking ID", required = true) @PathVariable Integer bookingId,
            @Parameter(description = "Guest ID", required = true) @PathVariable Integer guestId) {
        
        log.info("Received GET request for booking confirmation details: bookingId={}, guestId={}",
                bookingId, guestId);
        
        BookingConfirmationRequest request = new BookingConfirmationRequest(bookingId, guestId);
        BookingConfirmationDetailsResponse response = 
                bookingConfirmationService.getBookingConfirmationDetails(request);
        
        if (Boolean.FALSE.equals(response.getSuccess())) {
            log.warn("Failed to retrieve booking confirmation details: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
} 