package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.BookingDetailsDTO;
import com.kdu.rizzlers.dto.TravelItineraryDto;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EmailServiceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = EmailService.class;

        // Test sendTravelItineraryEmail with TravelItineraryDto
        try {
            Method method = interfaceClass.getMethod("sendTravelItineraryEmail", TravelItineraryDto.class, String.class);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'sendTravelItineraryEmail(TravelItineraryDto, String)' not found or signature mismatch", e);
        }

        // Test sendTravelItineraryEmail with BookingConfirmationDetailsResponse
        try {
            Method method = interfaceClass.getMethod("sendTravelItineraryEmail", BookingConfirmationDetailsResponse.class, String.class);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'sendTravelItineraryEmail(BookingConfirmationDetailsResponse, String)' not found or signature mismatch", e);
        }

        // Test sendOtpEmail
        try {
            Method method = interfaceClass.getMethod("sendOtpEmail", String.class, String.class, BookingDetailsDTO.class);
            assertNotNull(method);
            assertEquals(void.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'sendOtpEmail' not found or signature mismatch", e);
        }

        // Test sendMyBookingsOtpEmail
        try {
            Method method = interfaceClass.getMethod("sendMyBookingsOtpEmail", String.class, String.class, String.class, String.class);
            assertNotNull(method);
            assertEquals(void.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'sendMyBookingsOtpEmail' not found or signature mismatch", e);
        }

        // Test sendReviewInvitationEmail
        try {
            Method method = interfaceClass.getMethod("sendReviewInvitationEmail", String.class, String.class, String.class, String.class, int.class);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'sendReviewInvitationEmail' not found or signature mismatch", e);
        }
    }
} 