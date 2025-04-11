package com.kdu.rizzlers.scheduler;

import com.kdu.rizzlers.service.impl.OtpCleanupServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for OTP cleanup tasks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {

    private final OtpCleanupServiceImpl otpCleanupService;

    /**
     * Scheduled task to clear expired OTPs every hour
     * This task runs at the beginning of every hour (minute 0)
     */
    @Scheduled(cron = "0 0 * * * *") // Run at minute 0 of every hour
    public void scheduledOtpCleanup() {
        log.info("Starting scheduled OTP cleanup task");
        try {
            int clearedCount = otpCleanupService.clearExpiredOtps();
            log.info("Scheduled OTP cleanup completed. Cleared {} expired OTPs", clearedCount);
        } catch (Exception e) {
            log.error("Error during scheduled OTP cleanup: {}", e.getMessage(), e);
        }
    }
} 