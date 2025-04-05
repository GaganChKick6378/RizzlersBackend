package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.BookingRequest;
import com.kdu.rizzlers.dto.BookingResponse;
import com.kdu.rizzlers.entity.BookingLock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for booking operations
 */
public interface BookingService {

    /**
     * Book a room with the given booking request
     * 
     * @param bookingRequest the booking request details
     * @return BookingResponse with the booking details or failure information
     */
    BookingResponse bookRoom(BookingRequest bookingRequest);
    
    /**
     * Acquire a lock on a room for the booking process
     * 
     * @param roomId the room ID to lock
     * @param propertyId the property ID
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @param sessionId a unique identifier for the session/user
     * @return Optional of BookingLock if successfully acquired, otherwise empty
     */
    Optional<BookingLock> acquireRoomLock(
            Integer roomId,
            Integer propertyId,
            LocalDate startDate,
            LocalDate endDate,
            String sessionId);
    
    /**
     * Release a room lock
     * 
     * @param lockId the ID of the lock to release
     * @return true if successfully released, false otherwise
     */
    boolean releaseRoomLock(Long lockId);
    
    /**
     * Get a list of available room IDs for booking based on the available rooms that are not locked
     * 
     * @param availableRoomIds the list of room IDs available from room availability service
     * @param propertyId the property ID
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @return list of room IDs that are available for booking
     */
    List<Integer> filterLockedRooms(
            List<Integer> availableRoomIds,
            Integer propertyId,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Clean up expired locks
     * 
     * @return number of locks cleaned up
     */
    int cleanupExpiredLocks();
    
    /**
     * Create a booking in the GraphQL database
     * 
     * @param bookingRequest the booking request details
     * @param roomId the selected room ID
     * @return booking ID if successful, or empty optional if failed
     */
    Optional<Integer> createBookingInGraphQL(BookingRequest bookingRequest, Integer roomId);
    
    /**
     * Update room availabilities in GraphQL to associate with the booking
     * 
     * @param bookingId the booking ID
     * @param roomId the room ID
     * @param propertyId the property ID
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @return true if successful, false otherwise
     */
    boolean updateRoomAvailabilities(
            Integer bookingId,
            Integer roomId,
            Integer propertyId,
            LocalDate startDate,
            LocalDate endDate);
} 