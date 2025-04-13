package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.RoomBookingDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for retrieving room booking data from GraphQL
 */
public interface RoomBookingService {
    
    /**
     * Get rooms with check-in on a specific date
     * 
     * @param propertyId the property ID
     * @param date the check-in date
     * @return List of rooms with check-in on the date
     */
    List<RoomBookingDTO> getRoomsWithCheckInOnDate(Integer propertyId, LocalDate date);
    
    /**
     * Get rooms with check-out on a specific date
     * 
     * @param propertyId the property ID
     * @param date the check-out date
     * @return List of rooms with check-out on the date
     */
    List<RoomBookingDTO> getRoomsWithCheckOutOnDate(Integer propertyId, LocalDate date);
    
    /**
     * Get occupied rooms for a property (rooms currently booked)
     * 
     * @param propertyId the property ID
     * @param date the current date
     * @return List of occupied rooms
     */
    List<RoomBookingDTO> getOccupiedRooms(Integer propertyId, LocalDate date);
} 