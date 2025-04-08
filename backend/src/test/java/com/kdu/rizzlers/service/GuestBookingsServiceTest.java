package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpRequestDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpVerificationDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GuestBookingsServiceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = GuestBookingsService.class;

        // Test requestOtp method
        try {
            Method method = interfaceClass.getMethod("requestOtp", MyBookingsOtpRequestDTO.class);
            assertNotNull(method);
            assertEquals(GuestBookingsResponseDTO.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'requestOtp' not found or signature mismatch", e);
        }

        // Test verifyOtpAndGetBookings method
        try {
            Method method = interfaceClass.getMethod("verifyOtpAndGetBookings", MyBookingsOtpVerificationDTO.class);
            assertNotNull(method);
            assertEquals(GuestBookingsResponseDTO.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'verifyOtpAndGetBookings' not found or signature mismatch", e);
        }
    }
} 