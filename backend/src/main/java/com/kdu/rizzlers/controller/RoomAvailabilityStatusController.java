package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.RoomAvailabilityStatusRequestDTO;
import com.kdu.rizzlers.dto.out.RoomAvailabilityStatusResponseDTO;
import com.kdu.rizzlers.service.RoomAvailabilityStatusService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for room availability status API
 */
@Slf4j
@RestController
@RequestMapping("/checkout/availability-status")
@RequiredArgsConstructor
@Tag(name = "Room Availability", description = "APIs for checking room availability status")
public class RoomAvailabilityStatusController {

    private final RoomAvailabilityStatusService roomAvailabilityStatusService;
    
    @PostMapping("/check-with-promotion")
    @Operation(
        summary = "Check room availability with promotion",
        description = "Checks if rooms are available for the specified dates with promotion applied"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Availability check completed successfully",
            content = @Content(schema = @Schema(implementation = RoomAvailabilityStatusResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters"
        )
    })
    public ResponseEntity<RoomAvailabilityStatusResponseDTO> checkRoomAvailabilityStatusWithPromotion(
            @Parameter(description = "Room availability request with promotion details", required = true)
            @RequestBody RoomAvailabilityStatusRequestDTO request) {
            
        log.info("Received request to check room availability status with promotion {}: propertyId={}, roomTypeId={}, startDate={}, endDate={}",
                request.getPromotionId(), request.getPropertyId(), request.getRoomTypeId(), request.getStartDate(), request.getEndDate());
        
        RoomAvailabilityStatusResponseDTO response = roomAvailabilityStatusService
                .checkRoomAvailabilityStatusWithPromotion(request, request.getPromotionId());
        
        log.info("Room availability check with promotion result: available={}, roomTypeId={}, availableCount={}/{}, promoDiscount={}",
                response.isAvailable(), response.getRoomTypeId(), 
                response.getAvailableRoomCount(), response.getRequestedRoomCount(),
                response.getPromoDiscount());
        
        return ResponseEntity.ok(response);
    }
} 