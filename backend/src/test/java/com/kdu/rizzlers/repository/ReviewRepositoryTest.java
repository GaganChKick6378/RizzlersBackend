package com.kdu.rizzlers.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReviewRepository reviewRepository;

    private Map<String, Object> sampleReview;
    private List<String> sampleAmenities;

    @BeforeEach
    void setUp() {
        sampleReview = new HashMap<>();
        sampleReview.put("rating", 4.5);
        sampleReview.put("description", "Great room with a view");
        sampleReview.put("images", "{\"image1.jpg\",\"image2.jpg\"}");
        sampleReview.put("review_count", 10);

        sampleAmenities = Arrays.asList("WiFi", "Pool", "Gym");
    }

    @Test
    @DisplayName("Should find review by room type ID")
    void findReviewByRoomTypeId() {
        // Arrange
        Integer roomTypeId = 123;
        List<Map<String, Object>> results = List.of(sampleReview);
        when(jdbcTemplate.queryForList(anyString(), eq(roomTypeId))).thenReturn(results);

        // Act
        Optional<Map<String, Object>> result = reviewRepository.findReviewByRoomTypeId(roomTypeId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(4.5, result.get().get("rating"));
        assertEquals("Great room with a view", result.get().get("description"));
        assertEquals("{\"image1.jpg\",\"image2.jpg\"}", result.get().get("images"));
        assertEquals(10, result.get().get("review_count"));
    }

    @Test
    @DisplayName("Should return empty optional when no review found")
    void findReviewByRoomTypeId_noReviewFound() {
        // Arrange
        Integer roomTypeId = 456;
        when(jdbcTemplate.queryForList(anyString(), eq(roomTypeId))).thenReturn(List.of());

        // Act
        Optional<Map<String, Object>> result = reviewRepository.findReviewByRoomTypeId(roomTypeId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find amenities by room type ID")
    void findAmenitiesByRoomTypeId() {
        // Arrange
        Integer roomTypeId = 123;
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq(roomTypeId))).thenReturn(sampleAmenities);

        // Act
        List<String> result = reviewRepository.findAmenitiesByRoomTypeId(roomTypeId);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains("WiFi"));
        assertTrue(result.contains("Pool"));
        assertTrue(result.contains("Gym"));
    }

    @Test
    @DisplayName("Should parse text array correctly")
    void parseTextArray() {
        // Arrange
        String textArray = "{\"WiFi\",\"Pool\",\"Gym\"}";

        // Act
        List<String> result = reviewRepository.parseTextArray(textArray);

        // Assert
        assertEquals(3, result.size());
        assertEquals("WiFi", result.get(0));
        assertEquals("Pool", result.get(1));
        assertEquals("Gym", result.get(2));
    }

    @Test
    @DisplayName("Should handle empty text array")
    void parseTextArray_empty() {
        // Arrange & Act
        List<String> result1 = reviewRepository.parseTextArray("{}");
        List<String> result2 = reviewRepository.parseTextArray("");
        List<String> result3 = reviewRepository.parseTextArray(null);

        // Assert
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
    }
} 