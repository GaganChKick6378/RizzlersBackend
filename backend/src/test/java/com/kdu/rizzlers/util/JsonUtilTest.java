package com.kdu.rizzlers.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {

    @Test
    public void testJsonToMap_ValidJson() {
        // Arrange
        String json = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        
        // Act
        Map<String, Object> result = JsonUtil.jsonToMap(json);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
        assertEquals("New York", result.get("city"));
    }

    @Test
    public void testJsonToMap_ComplexJson() {
        // Arrange
        String json = "{\"name\":\"John\",\"address\":{\"street\":\"123 Main St\",\"city\":\"New York\"},\"phones\":[\"555-1234\",\"555-5678\"]}";
        
        // Act
        Map<String, Object> result = JsonUtil.jsonToMap(json);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("John", result.get("name"));
        
        // Check nested object
        assertTrue(result.get("address") instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) result.get("address");
        assertEquals("123 Main St", address.get("street"));
        assertEquals("New York", address.get("city"));
        
        // Check array
        assertTrue(result.get("phones") instanceof java.util.List);
        @SuppressWarnings("unchecked")
        java.util.List<String> phones = (java.util.List<String>) result.get("phones");
        assertEquals(2, phones.size());
        assertEquals("555-1234", phones.get(0));
        assertEquals("555-5678", phones.get(1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    public void testJsonToMap_NullOrEmptyOrWhitespace(String input) {
        // Act
        Map<String, Object> result = JsonUtil.jsonToMap(input);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testJsonToMap_InvalidJson() {
        // Arrange
        String invalidJson = "{name:John,age:30}"; // Missing quotes around keys
        
        // Act
        Map<String, Object> result = JsonUtil.jsonToMap(invalidJson);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testJsonToMap_ArrayJson() {
        // Arrange
        String arrayJson = "[1,2,3,4]";
        
        // Act
        Map<String, Object> result = JsonUtil.jsonToMap(arrayJson);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should return empty map for array JSON
    }
} 