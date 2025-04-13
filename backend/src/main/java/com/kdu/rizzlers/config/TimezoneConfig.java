package com.kdu.rizzlers.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuration class that sets the default timezone for the application
 */
@Configuration
@Slf4j
public class TimezoneConfig {

    private static final ZoneId SYSTEM_TIMEZONE = ZoneId.of("UTC");

    /**
     * Set the default timezone for the application to prevent timezone conversion issues
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(SYSTEM_TIMEZONE));
        log.info("Setting default timezone to: {}", SYSTEM_TIMEZONE);
        
        // Log the property timezone for reference
        ZoneId propertyTimezone = ZoneId.of("Asia/Kolkata");
        log.info("Property timezone (Asia/Kolkata) offset from UTC: {}", propertyTimezone.getRules().getOffset(java.time.Instant.now()));
    }
} 