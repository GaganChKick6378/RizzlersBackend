package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.BookingConfirmationRequest;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BookingConfirmationServiceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = BookingConfirmationService.class;

        // Test getBookingConfirmationDetails method
        try {
            Method method = interfaceClass.getMethod("getBookingConfirmationDetails", BookingConfirmationRequest.class);
            assertNotNull(method);
            assertEquals(BookingConfirmationDetailsResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getBookingConfirmationDetails' not found or signature mismatch", e);
        }
    }
} 