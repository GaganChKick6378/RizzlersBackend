package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.RoomAvailabilityStatusRequestDTO;
import com.kdu.rizzlers.dto.out.RoomAvailabilityStatusResponseDTO;

/**
 * Service for checking room availability status and detailed pricing
 */
public interface RoomAvailabilityStatusService {
    
    /**
     * Checks the availability status of rooms based on the provided criteria
     * and returns detailed pricing information for each day in the date range.
     * 
     * @param request The request containing all criteria for availability check
     * @return A response with availability status, room details and per-day pricing
     */
    RoomAvailabilityStatusResponseDTO checkRoomAvailabilityStatus(RoomAvailabilityStatusRequestDTO request);
    
    /**
     * Checks the availability status of rooms based on the provided criteria
     * and returns detailed pricing information with promotion discount applied.
     * This method identifies if the promotion comes from GraphQL (ends with 001) 
     * or from RDS database (ends with 002) and applies the appropriate price factor.
     * 
     * @param request The request containing all criteria for availability check
     * @param promotionId The promotion ID (with source suffix)
     * @return A response with availability status, room details, per-day pricing, and promotion details
     */
    RoomAvailabilityStatusResponseDTO checkRoomAvailabilityStatusWithPromotion(
            RoomAvailabilityStatusRequestDTO request, Integer promotionId);
} 