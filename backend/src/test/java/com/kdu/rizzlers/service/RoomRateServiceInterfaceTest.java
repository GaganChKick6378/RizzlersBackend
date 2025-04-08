package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RoomRateServiceInterfaceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = RoomRateService.class;

        // Test getActivePromotions method
        try {
            Method method = interfaceClass.getMethod("getActivePromotions", Integer.class, LocalDate.class, LocalDate.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getActivePromotions' not found or signature mismatch", e);
        }

        // Test getAllPromotions method
        try {
            Method method = interfaceClass.getMethod("getAllPromotions", Integer.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAllPromotions' not found or signature mismatch", e);
        }

        // Test getDailyRatesWithPromotions method
        try {
            Method method = interfaceClass.getMethod("getDailyRatesWithPromotions", Integer.class, Integer.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getDailyRatesWithPromotions' not found or signature mismatch", e);
        }
    }
} 