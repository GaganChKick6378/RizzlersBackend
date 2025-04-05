package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.RoomAvailabilityStatusRequestDTO;
import com.kdu.rizzlers.dto.out.RoomAvailabilityStatusResponseDTO;
import com.kdu.rizzlers.service.RoomAvailabilityStatusService;
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
public class RoomAvailabilityStatusController {

    private final RoomAvailabilityStatusService roomAvailabilityStatusService;
    
    @PostMapping("/check-with-promotion")
    public ResponseEntity<RoomAvailabilityStatusResponseDTO> checkRoomAvailabilityStatusWithPromotion(
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