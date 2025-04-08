package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.TenantPropertyAssignmentRequest;
import com.kdu.rizzlers.dto.out.TenantPropertyAssignmentResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TenantPropertyAssignmentServiceInterfaceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = TenantPropertyAssignmentService.class;

        // Test createAssignment method
        try {
            Method method = interfaceClass.getMethod("createAssignment", TenantPropertyAssignmentRequest.class);
            assertNotNull(method);
            assertEquals(TenantPropertyAssignmentResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'createAssignment' not found or signature mismatch", e);
        }

        // Test getAssignmentById method
        try {
            Method method = interfaceClass.getMethod("getAssignmentById", Long.class);
            assertNotNull(method);
            assertEquals(TenantPropertyAssignmentResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAssignmentById' not found or signature mismatch", e);
        }

        // Test getAllAssignments method
        try {
            Method method = interfaceClass.getMethod("getAllAssignments");
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAllAssignments' not found or signature mismatch", e);
        }

        // Test getAssignmentsByTenantId method
        try {
            Method method = interfaceClass.getMethod("getAssignmentsByTenantId", Integer.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAssignmentsByTenantId' not found or signature mismatch", e);
        }

        // Test getAssignedPropertiesByTenantId method
        try {
            Method method = interfaceClass.getMethod("getAssignedPropertiesByTenantId", Integer.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAssignedPropertiesByTenantId' not found or signature mismatch", e);
        }

        // Test getAssignmentsByPropertyId method
        try {
            Method method = interfaceClass.getMethod("getAssignmentsByPropertyId", Integer.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAssignmentsByPropertyId' not found or signature mismatch", e);
        }

        // Test getAssignmentByTenantIdAndPropertyId method
        try {
            Method method = interfaceClass.getMethod("getAssignmentByTenantIdAndPropertyId", Integer.class, Integer.class);
            assertNotNull(method);
            assertEquals(TenantPropertyAssignmentResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAssignmentByTenantIdAndPropertyId' not found or signature mismatch", e);
        }

        // Test updateAssignment method
        try {
            Method method = interfaceClass.getMethod("updateAssignment", Long.class, TenantPropertyAssignmentRequest.class);
            assertNotNull(method);
            assertEquals(TenantPropertyAssignmentResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'updateAssignment' not found or signature mismatch", e);
        }

        // Test deleteAssignment method
        try {
            Method method = interfaceClass.getMethod("deleteAssignment", Long.class);
            assertNotNull(method);
            assertEquals(void.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'deleteAssignment' not found or signature mismatch", e);
        }
    }
} 