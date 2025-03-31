package com.kdu.rizzlers.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationValidatorTest {

    private ConfigurationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConfigurationValidator();
    }

    @Test
    @DisplayName("Header logo validation should pass with valid data")
    void validateHeaderLogo_withValidData_shouldReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("url", "https://example.com/logo.png");
        data.put("alt", "Logo alt text");

        // Act
        boolean result = validator.validateHeaderLogo(data);

        // Assert
        assertTrue(result);
        assertEquals("https://example.com/logo.png", data.get("url"));
        assertEquals("Logo alt text", data.get("alt"));
    }

    @Test
    @DisplayName("Header logo validation should add default alt text when missing")
    void validateHeaderLogo_withMissingAlt_shouldAddDefaultAndReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("url", "https://example.com/logo.png");

        // Act
        boolean result = validator.validateHeaderLogo(data);

        // Assert
        assertTrue(result);
        assertEquals("https://example.com/logo.png", data.get("url"));
        assertEquals("Logo", data.get("alt"));
    }

    @Test
    @DisplayName("Header logo validation should fail when URL is missing")
    void validateHeaderLogo_withMissingUrl_shouldReturnFalse() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("alt", "Logo alt text");

        // Act
        boolean result = validator.validateHeaderLogo(data);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Length of stay validation should pass with valid data")
    void validateLengthOfStay_withValidData_shouldReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("min", 1);
        data.put("max", 14);
        data.put("default", 3);

        // Act
        boolean result = validator.validateLengthOfStay(data);

        // Assert
        assertTrue(result);
        assertEquals(1, data.get("min"));
        assertEquals(14, data.get("max"));
        assertEquals(3, data.get("default"));
    }

    @Test
    @DisplayName("Length of stay validation should correct invalid values")
    void validateLengthOfStay_withInvalidValues_shouldCorrectValuesAndReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("min", 0);
        data.put("max", 0);
        data.put("default", 5);

        // Act
        boolean result = validator.validateLengthOfStay(data);

        // Assert
        assertTrue(result);
        assertEquals(1, data.get("min"));
        assertEquals(1, data.get("max"));
        assertEquals(1, data.get("default"));
    }

    @Test
    @DisplayName("Length of stay validation should fail when required fields are missing")
    void validateLengthOfStay_withMissingFields_shouldReturnFalse() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("min", 1);
        data.put("max", 14);
        // default is missing

        // Act
        boolean result = validator.validateLengthOfStay(data);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Length of stay validation should handle non-numeric values")
    void validateLengthOfStay_withNonNumericValues_shouldReturnFalse() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("min", "one");
        data.put("max", "fourteen");
        data.put("default", "three");

        // Act
        boolean result = validator.validateLengthOfStay(data);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Languages validation should pass with valid data")
    void validateLanguages_withValidData_shouldReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> options = new ArrayList<>();
        
        Map<String, Object> lang1 = new HashMap<>();
        lang1.put("code", "en");
        lang1.put("name", "English");
        lang1.put("active", true);
        
        Map<String, Object> lang2 = new HashMap<>();
        lang2.put("code", "es");
        lang2.put("name", "Spanish");
        lang2.put("active", false);
        
        options.add(lang1);
        options.add(lang2);
        
        data.put("options", options);
        data.put("default", "en");

        // Act
        boolean result = validator.validateLanguages(data);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Languages validation should add missing active field")
    void validateLanguages_withMissingActiveField_shouldAddDefaultAndReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> options = new ArrayList<>();
        
        Map<String, Object> language = new HashMap<>();
        language.put("code", "en");
        language.put("name", "English");
        
        options.add(language);
        data.put("options", options);
        data.put("default", "en");

        // Act
        boolean result = validator.validateLanguages(data);

        // Assert
        assertTrue(result);
        Map<String, Object> updatedLanguage = (Map<String, Object>) ((List<?>) data.get("options")).get(0);
        assertTrue((Boolean) updatedLanguage.get("active"));
    }

    @Test
    @DisplayName("Languages validation should handle invalid option entries")
    void validateLanguages_withInvalidOption_shouldHandleGracefully() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        List<Object> options = new ArrayList<>();
        options.add("This is not a map");
        
        Map<String, Object> validOption = new HashMap<>();
        validOption.put("code", "en");
        validOption.put("name", "English");
        options.add(validOption);
        
        data.put("options", options);
        data.put("default", "en");

        // Act
        boolean result = validator.validateLanguages(data);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Languages validation should fail with missing required fields")
    void validateLanguages_withMissingRequiredFields_shouldReturnFalse() {
        // Arrange - missing default field
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> options = new ArrayList<>();
        
        Map<String, Object> language = new HashMap<>();
        language.put("code", "en");
        language.put("name", "English");
        
        options.add(language);
        data.put("options", options);

        // Act
        boolean result = validator.validateLanguages(data);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Languages validation should fail when options is not a list")
    void validateLanguages_whenOptionsIsNotList_shouldReturnFalse() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("options", "not a list");
        data.put("default", "en");

        // Act
        boolean result = validator.validateLanguages(data);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Languages validation should fail when default is not a string")
    void validateLanguages_whenDefaultIsNotString_shouldReturnFalse() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> options = new ArrayList<>();
        
        Map<String, Object> language = new HashMap<>();
        language.put("code", "en");
        language.put("name", "English");
        
        options.add(language);
        data.put("options", options);
        data.put("default", 123);

        // Act
        boolean result = validator.validateLanguages(data);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Property image validation should pass with valid data")
    void validatePropertyImage_withValidData_shouldReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("url", "https://example.com/property.jpg");
        data.put("alt", "Property image");

        // Act
        boolean result = validator.validatePropertyImage(data);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Property image validation should add default alt text when missing")
    void validatePropertyImage_withMissingAlt_shouldAddDefaultAndReturnTrue() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("url", "https://example.com/property.jpg");

        // Act
        boolean result = validator.validatePropertyImage(data);

        // Assert
        assertTrue(result);
        assertEquals("Property Image", data.get("alt"));
    }

    @Test
    @DisplayName("Property image validation should fail when URL is missing")
    void validatePropertyImage_withMissingUrl_shouldReturnFalse() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("alt", "Property image");

        // Act
        boolean result = validator.validatePropertyImage(data);

        // Assert
        assertFalse(result);
    }
} 