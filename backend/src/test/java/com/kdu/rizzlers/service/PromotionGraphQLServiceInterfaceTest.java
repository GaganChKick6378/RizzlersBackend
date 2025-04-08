package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.PromotionDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PromotionGraphQLServiceInterfaceTest {

    @Test
    public void testInterfaceMethods() {
        Class<?> interfaceClass = PromotionGraphQLService.class;

        // Test fetchAllPromotions method
        try {
            Method method = interfaceClass.getMethod("fetchAllPromotions");
            assertNotNull(method);
            assertEquals(List.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'fetchAllPromotions' not found or signature mismatch", e);
        }

        // Test fetchPromotion method
        try {
            Method method = interfaceClass.getMethod("fetchPromotion", Integer.class);
            assertNotNull(method);
            assertEquals(Map.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method 'fetchPromotion' not found or signature mismatch", e);
        }
    }
} 