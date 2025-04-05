package com.kdu.rizzlers.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for checking room availability
 */
public interface RoomAvailabilityCheckService {

    /**
     * Checks if rooms of a specific type are available for the given criteria
     * 
     * @param propertyId the property ID
     * @param roomTypeId the room type ID
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @param roomCount the number of rooms required
     * @return a map containing room type details and availability information, or empty map if not available
     */
    Map<String, Object> checkRoomTypeAvailability(
            Integer propertyId,
            Integer roomTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Integer roomCount);
    
    /**
     * Fetches rooms by room type and property ID
     * 
     * @param propertyId the property ID
     * @param roomTypeId the room type ID
     * @return a list of room details
     */
    List<Map<String, Object>> fetchRoomsByTypeAndProperty(
            Integer propertyId,
            Integer roomTypeId);
    
    /**
     * Fetches available room IDs for the given criteria
     * 
     * @param propertyId the property ID
     * @param roomIds the list of room IDs to check
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @return a set of available room IDs
     */
    Set<Integer> fetchAvailableRoomIds(
            Integer propertyId,
            List<Integer> roomIds,
            LocalDate startDate,
            LocalDate endDate);
} 