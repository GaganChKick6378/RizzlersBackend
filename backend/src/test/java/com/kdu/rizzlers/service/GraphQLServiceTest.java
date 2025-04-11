package com.kdu.rizzlers.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GraphQLServiceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = GraphQLService.class;

        // Test executeQuery method without variables
        try {
            Method method = interfaceClass.getMethod("executeQuery", String.class);
            assertNotNull(method);
            assertEquals(Map.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'executeQuery(String)' not found or signature mismatch", e);
        }

        // Test executeQuery method with variables
        try {
            Method method = interfaceClass.getMethod("executeQuery", String.class, Map.class);
            assertNotNull(method);
            assertEquals(Map.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'executeQuery(String, Map)' not found or signature mismatch", e);
        }
    }
} 