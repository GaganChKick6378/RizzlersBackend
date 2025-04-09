package com.kdu.rizzlers.scheduler;

import com.kdu.rizzlers.service.EmailOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to clean up expired OTPs
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailOtpCleanupScheduler {

    private final EmailOtpService emailOtpService;
    
    /**
     * Scheduled task to clean up expired OTPs
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Run at the top of every hour
    public void cleanupExpiredOtps() {
        log.info("Running scheduled email OTP cleanup task");
        int deletedCount = emailOtpService.cleanupExpiredOtps();
        log.info("Email OTP cleanup task completed. Removed {} expired records", deletedCount);
    }
} 