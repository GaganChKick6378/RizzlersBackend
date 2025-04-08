package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.GuestTypeDefinitionRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GuestTypeDefinitionServiceInterfaceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = GuestTypeDefinitionService.class;

        // Test createGuestTypeDefinition method
        try {
            Method method = interfaceClass.getMethod("createGuestTypeDefinition", GuestTypeDefinitionRequest.class);
            assertNotNull(method);
            assertEquals(GuestTypeDefinitionResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'createGuestTypeDefinition' not found or signature mismatch", e);
        }

        // Test getGuestTypeDefinitionById method
        try {
            Method method = interfaceClass.getMethod("getGuestTypeDefinitionById", Long.class);
            assertNotNull(method);
            assertEquals(GuestTypeDefinitionResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getGuestTypeDefinitionById' not found or signature mismatch", e);
        }

        // Test getAllGuestTypeDefinitions method
        try {
            Method method = interfaceClass.getMethod("getAllGuestTypeDefinitions");
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getAllGuestTypeDefinitions' not found or signature mismatch", e);
        }

        // Test getGuestTypeDefinitionsByTenantIdAndIsActive method
        try {
            Method method = interfaceClass.getMethod("getGuestTypeDefinitionsByTenantIdAndIsActive", Integer.class, Boolean.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getGuestTypeDefinitionsByTenantIdAndIsActive' not found or signature mismatch", e);
        }

        // Test getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive method
        try {
            Method method = interfaceClass.getMethod("getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive", 
                    Integer.class, String.class, Boolean.class);
            assertNotNull(method);
            assertEquals(GuestTypeDefinitionResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive' not found or signature mismatch", e);
        }

        // Test getGuestTypeDefinitionsByTenantId method
        try {
            Method method = interfaceClass.getMethod("getGuestTypeDefinitionsByTenantId", Integer.class);
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'getGuestTypeDefinitionsByTenantId' not found or signature mismatch", e);
        }

        // Test updateGuestTypeDefinition method
        try {
            Method method = interfaceClass.getMethod("updateGuestTypeDefinition", Long.class, GuestTypeDefinitionRequest.class);
            assertNotNull(method);
            assertEquals(GuestTypeDefinitionResponse.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'updateGuestTypeDefinition' not found or signature mismatch", e);
        }

        // Test deleteGuestTypeDefinition method
        try {
            Method method = interfaceClass.getMethod("deleteGuestTypeDefinition", Long.class);
            assertNotNull(method);
            assertEquals(void.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'deleteGuestTypeDefinition' not found or signature mismatch", e);
        }
    }
} 