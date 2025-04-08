package com.kdu.rizzlers.scheduler;

import com.kdu.rizzlers.service.impl.OtpCleanupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OtpCleanupSchedulerTest {

    @Mock
    private OtpCleanupServiceImpl otpCleanupService;

    @InjectMocks
    private OtpCleanupScheduler otpCleanupScheduler;

    @BeforeEach
    public void setup() {
        // No setup needed for now
    }

    @Test
    public void testScheduledOtpCleanup_Success() {
        // Arrange
        when(otpCleanupService.clearExpiredOtps()).thenReturn(5);

        // Act
        otpCleanupScheduler.scheduledOtpCleanup();

        // Assert
        verify(otpCleanupService, times(1)).clearExpiredOtps();
    }

    @Test
    public void testScheduledOtpCleanup_Exception() {
        // Arrange
        when(otpCleanupService.clearExpiredOtps()).thenThrow(new RuntimeException("Service error"));

        // Act
        otpCleanupScheduler.scheduledOtpCleanup();

        // Assert
        verify(otpCleanupService, times(1)).clearExpiredOtps();
        // No exception should be thrown outside the method
    }

    @Test
    public void testScheduledOtpCleanup_NoExpiredOtps() {
        // Arrange
        when(otpCleanupService.clearExpiredOtps()).thenReturn(0);

        // Act
        otpCleanupScheduler.scheduledOtpCleanup();

        // Assert
        verify(otpCleanupService, times(1)).clearExpiredOtps();
    }
} 