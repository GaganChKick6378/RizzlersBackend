package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.RoomAvailabilityStatusRequestDTO;
import com.kdu.rizzlers.dto.out.RoomAvailabilityStatusResponseDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RoomAvailabilityStatusServiceInterfaceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = RoomAvailabilityStatusService.class;

        // Test checkRoomAvailabilityStatus method
        try {
            Method method = interfaceClass.getMethod("checkRoomAvailabilityStatus", RoomAvailabilityStatusRequestDTO.class);
            assertNotNull(method);
            assertEquals(RoomAvailabilityStatusResponseDTO.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'checkRoomAvailabilityStatus' not found or signature mismatch", e);
        }

        // Test checkRoomAvailabilityStatusWithPromotion method
        try {
            Method method = interfaceClass.getMethod("checkRoomAvailabilityStatusWithPromotion", 
                    RoomAvailabilityStatusRequestDTO.class, Integer.class);
            assertNotNull(method);
            assertEquals(RoomAvailabilityStatusResponseDTO.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'checkRoomAvailabilityStatusWithPromotion' not found or signature mismatch", e);
        }
    }
} 