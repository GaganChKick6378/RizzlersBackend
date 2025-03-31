package com.kdu.rizzlers.dto.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PageResponse class
 */
class PageResponseTest {

    @Test
    @DisplayName("Constructor and getters should work correctly")
    void testConstructorAndGetters() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2");
        int pageNumber = 1;
        int pageSize = 10;
        long totalElements = 100;
        int totalPages = 10;
        boolean last = false;

        // Act
        PageResponse<String> response = new PageResponse<>(content, pageNumber, pageSize, totalElements, totalPages, last);

        // Assert
        assertEquals(content, response.getContent());
        assertEquals(pageNumber, response.getPageNumber());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(totalElements, response.getTotalElements());
        assertEquals(totalPages, response.getTotalPages());
        assertEquals(last, response.isLast());
    }

    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilder() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2");
        int pageNumber = 1;
        int pageSize = 10;
        long totalElements = 100;
        int totalPages = 10;
        boolean last = false;

        // Act
        PageResponse<String> response = PageResponse.<String>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .build();

        // Assert
        assertEquals(content, response.getContent());
        assertEquals(pageNumber, response.getPageNumber());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(totalElements, response.getTotalElements());
        assertEquals(totalPages, response.getTotalPages());
        assertEquals(last, response.isLast());
    }

    @Test
    @DisplayName("Static factory method 'of' should create correct instance with pagination")
    void testOfMethodWithPagination() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        int pageNumber = 1;
        int pageSize = 2;

        // Act
        PageResponse<String> response = PageResponse.of(content, pageNumber, pageSize);

        // Assert
        assertEquals(2, response.getContent().size());
        assertEquals("item3", response.getContent().get(0));
        assertEquals("item4", response.getContent().get(1));
        assertEquals(pageNumber, response.getPageNumber());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isLast());
    }

    @Test
    @DisplayName("Static factory method 'of' should handle null content")
    void testOfMethodWithNullContent() {
        // Act
        PageResponse<String> response = PageResponse.of(null, 0, 10);

        // Assert
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isLast());
    }

    @Test
    @DisplayName("Static factory method 'of' should handle empty content")
    void testOfMethodWithEmptyContent() {
        // Act
        PageResponse<String> response = PageResponse.of(Collections.emptyList(), 0, 10);

        // Assert
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isLast());
    }

    @Test
    @DisplayName("Static factory method 'of' should handle negative page number")
    void testOfMethodWithNegativePage() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2", "item3");

        // Act
        PageResponse<String> response = PageResponse.of(content, -1, 10);

        // Assert
        assertEquals(0, response.getPageNumber()); // Should default to 0
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());
    }

    @Test
    @DisplayName("Static factory method 'of' should handle zero or negative page size")
    void testOfMethodWithInvalidPageSize() {
        // Arrange
        List<String> content = Arrays.asList("item1", "item2", "item3");

        // Act
        PageResponse<String> response = PageResponse.of(content, 1, 0);

        // Assert
        assertEquals(1, response.getPageNumber());
        assertEquals(10, response.getPageSize()); // Should default to 10
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());
    }

    @Test
    @DisplayName("Last page flag should be correctly set")
    void testLastPageFlag() {
        // Arrange & Act - last page
        PageResponse<String> lastPageResponse = PageResponse.of(
                Arrays.asList("item1", "item2", "item3", "item4", "item5"), 2, 2);

        // Assert
        assertTrue(lastPageResponse.isLast());

        // Arrange & Act - not last page
        PageResponse<String> notLastPageResponse = PageResponse.of(
                Arrays.asList("item1", "item2", "item3", "item4", "item5"), 0, 2);

        // Assert
        assertFalse(notLastPageResponse.isLast());
    }

    @Test
    @DisplayName("Equals and HashCode contracts should be satisfied")
    void testEqualsAndHashCode() {
        // Arrange
        List<String> content = new ArrayList<>(Arrays.asList("item1", "item2"));
        PageResponse<String> response1 = PageResponse.of(content, 1, 10);
        PageResponse<String> response2 = PageResponse.of(content, 1, 10);
        PageResponse<String> response3 = PageResponse.of(
                Arrays.asList("item1", "item2", "item3"), 1, 10);

        // Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }
} 