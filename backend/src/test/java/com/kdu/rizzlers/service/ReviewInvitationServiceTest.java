package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.entity.ReviewInvitation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReviewInvitationServiceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = ReviewInvitationService.class;

        // Test sendReviewInvitationsForCheckoutDate method
        try {
            Method method = interfaceClass.getMethod("sendReviewInvitationsForCheckoutDate", LocalDate.class);
            assertNotNull(method);
            assertEquals(int.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'sendReviewInvitationsForCheckoutDate' not found or signature mismatch", e);
        }

        // Test validateToken method
        try {
            Method method = interfaceClass.getMethod("validateToken", String.class);
            assertNotNull(method);
            assertEquals(ReviewValidationResponseDTO.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'validateToken' not found or signature mismatch", e);
        }

        // Test markInvitationCompleted method
        try {
            Method method = interfaceClass.getMethod("markInvitationCompleted", String.class);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'markInvitationCompleted' not found or signature mismatch", e);
        }

        // Test findByToken method
        try {
            Method method = interfaceClass.getMethod("findByToken", String.class);
            assertNotNull(method);
            assertEquals(Optional.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'findByToken' not found or signature mismatch", e);
        }
    }
} 