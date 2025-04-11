package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OtpCleanupServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OtpCleanupServiceImpl otpCleanupService;

    @BeforeEach
    public void setup() {
        // No setup needed for now
    }

    @Test
    public void testClearExpiredOtps_Success() {
        // Arrange
        when(userRepository.clearExpiredOtps(any(ZonedDateTime.class))).thenReturn(5);

        // Act
        int clearedCount = otpCleanupService.clearExpiredOtps();

        // Assert
        assertEquals(5, clearedCount);
        verify(userRepository, times(1)).clearExpiredOtps(any(ZonedDateTime.class));
    }

    @Test
    public void testClearExpiredOtps_Exception() {
        // Arrange
        when(userRepository.clearExpiredOtps(any(ZonedDateTime.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        int clearedCount = otpCleanupService.clearExpiredOtps();

        // Assert
        assertEquals(0, clearedCount);
        verify(userRepository, times(1)).clearExpiredOtps(any(ZonedDateTime.class));
    }

    @Test
    public void testClearExpiredOtps_NoExpiredOtps() {
        // Arrange
        when(userRepository.clearExpiredOtps(any(ZonedDateTime.class))).thenReturn(0);

        // Act
        int clearedCount = otpCleanupService.clearExpiredOtps();

        // Assert
        assertEquals(0, clearedCount);
        verify(userRepository, times(1)).clearExpiredOtps(any(ZonedDateTime.class));
    }
} 