package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for cleaning up expired booking locks using scheduled tasks
 */
@Service
@Slf4j
public class BookingLockCleanupService {

    private final BookingService bookingService;

    public BookingLockCleanupService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Scheduled task to clean up expired locks every minute
     * Deletes expired locks from the database instead of marking them as EXPIRED
     */
    @Scheduled(fixedRate = 60000) // Run every 1 minute (60,000 ms)
    public void scheduledLockCleanup() {
        try {
            log.info("Running scheduled booking lock cleanup");
            int processedCount = bookingService.cleanupExpiredLocks();
            log.info("Processed {} expired locks", processedCount);
        } catch (Exception e) {
            log.error("Error during scheduled lock cleanup: {}", e.getMessage(), e);
        }
    }
} 