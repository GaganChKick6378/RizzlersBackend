package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.BookingRequest;
import com.kdu.rizzlers.dto.BookingResponse;
import com.kdu.rizzlers.entity.BookingLock;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookingServiceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = BookingService.class;

        // Test bookRoom method
        try {
            Method method = interfaceClass.getMethod("bookRoom", BookingRequest.class);
            assertNotNull(method);
            assertEquals(BookingResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'bookRoom' not found or signature mismatch", e);
        }

        // Test acquireRoomLock method
        try {
            Method method = interfaceClass.getMethod("acquireRoomLock", Integer.class, Integer.class, 
                    LocalDate.class, LocalDate.class, String.class);
            assertNotNull(method);
            assertEquals(Optional.class, method.getReturnType());
            // Check generic type if needed
            // ParameterizedType paramType = (ParameterizedType) method.getGenericReturnType();
            // assertEquals(BookingLock.class, paramType.getActualTypeArguments()[0]);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'acquireRoomLock' not found or signature mismatch", e);
        }

        // Test releaseRoomLock method
        try {
            Method method = interfaceClass.getMethod("releaseRoomLock", Long.class);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'releaseRoomLock' not found or signature mismatch", e);
        }

        // Test filterLockedRooms method
        try {
            Method method = interfaceClass.getMethod("filterLockedRooms", List.class, Integer.class, 
                    LocalDate.class, LocalDate.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'filterLockedRooms' not found or signature mismatch", e);
        }

        // Test cleanupExpiredLocks method
        try {
            Method method = interfaceClass.getMethod("cleanupExpiredLocks");
            assertNotNull(method);
            assertEquals(int.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'cleanupExpiredLocks' not found or signature mismatch", e);
        }

        // Test createBookingInGraphQL method
        try {
            Method method = interfaceClass.getMethod("createBookingInGraphQL", BookingRequest.class, Integer.class);
            assertNotNull(method);
            assertEquals(Optional.class, method.getReturnType());
            // Check generic type if needed
            // ParameterizedType paramType = (ParameterizedType) method.getGenericReturnType();
            // assertEquals(Integer.class, paramType.getActualTypeArguments()[0]);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'createBookingInGraphQL' not found or signature mismatch", e);
        }

        // Test updateRoomAvailabilities method
        try {
            Method method = interfaceClass.getMethod("updateRoomAvailabilities", Integer.class, Integer.class,
                    Integer.class, LocalDate.class, LocalDate.class);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'updateRoomAvailabilities' not found or signature mismatch", e);
        }
    }
} 