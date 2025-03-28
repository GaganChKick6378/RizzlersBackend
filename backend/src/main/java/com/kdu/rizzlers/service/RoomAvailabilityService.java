package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.common.PageResponse;
import com.kdu.rizzlers.dto.out.AvailableRoomDTO;

import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityService {
    
    /**
     * Get available rooms for a property based on dates and guest count
     * 
     * @param propertyId The ID of the property
     * @param startDate The check-in date
     * @param endDate The check-out date
     * @param guestCount Total number of guests
     * @param roomCount Number of rooms requested
     * @return List of available rooms with their details
     */
    List<AvailableRoomDTO> getAvailableRooms(
        Integer propertyId, 
        LocalDate startDate, 
        LocalDate endDate, 
        Integer guestCount,
        Integer roomCount
    );
    
    /**
     * Get paginated available rooms for a property based on dates and guest count
     * 
     * @param propertyId The ID of the property
     * @param startDate The check-in date
     * @param endDate The check-out date
     * @param guestCount Total number of guests
     * @param roomCount Number of rooms requested
     * @param pageNumber Page number (0-based)
     * @param pageSize Size of each page
     * @return Paginated list of available rooms with their details
     */
    PageResponse<AvailableRoomDTO> getAvailableRoomsPaginated(
        Integer propertyId, 
        LocalDate startDate, 
        LocalDate endDate, 
        Integer guestCount,
        Integer roomCount,
        int pageNumber,
        int pageSize
    );
} 