package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.BookingLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing booking locks with optimistic locking
 */
@Repository
public interface BookingLockRepository extends JpaRepository<BookingLock, Long>, JpaSpecificationExecutor<BookingLock> {

    /**
     * Find active booking lock for a specific room and date range.
     * Uses optimistic locking through @Version field in the entity.
     * 
     * @param roomId the room ID
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @param status the booking lock status
     * @return Optional of the booking lock if found
     */
    Optional<BookingLock> findByRoomIdAndStartDateAndEndDateAndStatus(
            Integer roomId, 
            LocalDate startDate, 
            LocalDate endDate, 
            BookingLock.BookingLockStatus status);
    
    /**
     * Find booking locks for a single room that overlap with the given date range
     * regardless of status
     *
     * @param roomId the room ID
     * @param endDate the end date
     * @param startDate the start date
     * @return List of overlapping booking locks
     */
    List<BookingLock> findByRoomIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Integer roomId,
            LocalDate endDate,
            LocalDate startDate);
    
    /**
     * Find all active booking locks for a list of room IDs
     * 
     * @param roomIds list of room IDs to check
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @param status the booking lock status
     * @return List of booking locks
     */
    List<BookingLock> findByRoomIdInAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            List<Integer> roomIds, 
            LocalDate endDate, 
            LocalDate startDate, 
            BookingLock.BookingLockStatus status);
    
    /**
     * Find all booking locks for a list of room IDs that overlap with the given date range
     * regardless of status
     *
     * @param roomIds list of room IDs to check
     * @param endDate the end date
     * @param startDate the start date
     * @return List of booking locks
     */
    List<BookingLock> findByRoomIdInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            List<Integer> roomIds,
            LocalDate endDate,
            LocalDate startDate);
    
    /**
     * Update status of expired booking locks
     * 
     * @param newStatus the new status to set
     * @param currentTime the current time
     * @param currentStatus the current status to look for
     * @return number of records updated
     */
    @Modifying
    @Query("UPDATE BookingLock bl SET bl.status = :newStatus, bl.statusUpdatedAt = :currentTime WHERE bl.lockExpiry < :currentTime AND bl.status = :currentStatus")
    int updateExpiredLocks(
            BookingLock.BookingLockStatus newStatus, 
            ZonedDateTime currentTime, 
            BookingLock.BookingLockStatus currentStatus);
    
    /**
     * Release a booking lock by ID
     * 
     * @param id the booking lock ID
     * @param status the new status
     * @return number of records updated
     */
    @Modifying
    @Query("UPDATE BookingLock bl SET bl.status = :status, bl.statusUpdatedAt = CURRENT_TIMESTAMP WHERE bl.id = :id")
    int releaseLock(Long id, BookingLock.BookingLockStatus status);
    
    /**
     * Delete a booking lock by ID
     * 
     * @param id the booking lock ID
     */
    @Modifying
    void deleteById(Long id);
    
    /**
     * Delete all expired booking locks with PENDING or CONFIRMED status
     * 
     * @param currentTime the current time
     * @return number of records deleted
     */
    @Modifying
    @Query("DELETE FROM BookingLock bl WHERE bl.lockExpiry < :currentTime AND bl.status IN (com.kdu.rizzlers.entity.BookingLock.BookingLockStatus.PENDING, com.kdu.rizzlers.entity.BookingLock.BookingLockStatus.CONFIRMED)")
    int deleteExpiredLocks(ZonedDateTime currentTime);
    
    /**
     * Find all expired locks with PENDING or CONFIRMED status
     * 
     * @param currentTime the current time
     * @return list of expired locks
     */
    @Query("SELECT bl FROM BookingLock bl WHERE bl.lockExpiry < :currentTime AND bl.status IN (com.kdu.rizzlers.entity.BookingLock.BookingLockStatus.PENDING, com.kdu.rizzlers.entity.BookingLock.BookingLockStatus.CONFIRMED)")
    List<BookingLock> findExpiredLocks(ZonedDateTime currentTime);
    
    /**
     * Find all CONFIRMED locks associated with a booking
     * 
     * @param bookingId the booking ID
     * @return list of confirmed locks
     */
    List<BookingLock> findByBookingIdAndStatus(Integer bookingId, BookingLock.BookingLockStatus status);
} 