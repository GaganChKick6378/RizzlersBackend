package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.BookingCancellationResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BookingCancellationServiceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = BookingCancellationService.class;

        // Test requestCancellation method
        try {
            Method method = interfaceClass.getMethod("requestCancellation", Integer.class, Integer.class);
            assertNotNull(method);
            assertEquals(BookingCancellationResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'requestCancellation' not found or signature mismatch", e);
        }

        // Test verifyCancellation method
        try {
            Method method = interfaceClass.getMethod("verifyCancellation", Integer.class, Integer.class, String.class);
            assertNotNull(method);
            assertEquals(BookingCancellationResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'verifyCancellation' not found or signature mismatch", e);
        }
    }
} 