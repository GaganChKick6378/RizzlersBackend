package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * Service implementation for handling OTP cleanup operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupServiceImpl {

    private final UserRepository userRepository;

    /**
     * Clears expired OTPs from the users table
     * This method finds all users with an OTP that has expired and sets their OTP to null
     * @return the number of records that were cleared
     */
    @Transactional
    public int clearExpiredOtps() {
        ZonedDateTime now = ZonedDateTime.now();
        log.info("Starting scheduled expired OTP cleanup task at {}", now);
        
        try {
            int clearedCount = userRepository.clearExpiredOtps(now);
            log.info("Successfully cleared {} expired OTPs", clearedCount);
            return clearedCount;
        } catch (Exception e) {
            log.error("Error clearing expired OTPs: {}", e.getMessage(), e);
            return 0;
        }
    }
} 