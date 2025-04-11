package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingLockCleanupServiceTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingLockCleanupService bookingLockCleanupService;

    @BeforeEach
    public void setup() {
        // No setup needed for now
    }

    @Test
    public void testScheduledLockCleanup_Success() {
        // Arrange
        when(bookingService.cleanupExpiredLocks()).thenReturn(5);

        // Act
        bookingLockCleanupService.scheduledLockCleanup();

        // Assert
        verify(bookingService, times(1)).cleanupExpiredLocks();
    }

    @Test
    public void testScheduledLockCleanup_Exception() {
        // Arrange
        when(bookingService.cleanupExpiredLocks()).thenThrow(new RuntimeException("DB error"));

        // Act
        bookingLockCleanupService.scheduledLockCleanup();

        // Assert
        verify(bookingService, times(1)).cleanupExpiredLocks();
    }

    @Test
    public void testScheduledLockCleanup_NoExpiredLocks() {
        // Arrange
        when(bookingService.cleanupExpiredLocks()).thenReturn(0);

        // Act
        bookingLockCleanupService.scheduledLockCleanup();

        // Assert
        verify(bookingService, times(1)).cleanupExpiredLocks();
    }
} 