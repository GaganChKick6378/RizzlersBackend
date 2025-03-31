package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterOptionsServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ReviewRepository reviewRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    @Spy
    private FilterOptionsServiceImpl filterOptionsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filterOptionsService, "graphqlEndpoint", "https://example.com/graphql");
        ReflectionTestUtils.setField(filterOptionsService, "apiKey", "test-api-key");
    }

    @Test
    @DisplayName("Should get amenities list from database")
    void getAllAmenities_shouldReturnAmenitiesList() {
        // Mock JdbcTemplate response
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(Arrays.asList("Pool", "Gym", "Spa"));

        // Execute method
        List<Map<String, String>> result = filterOptionsService.getAllAmenities();

        // Verify
        assertEquals(3, result.size());
        assertEquals("pool", result.get(0).get("id"));
        assertEquals("Pool", result.get(0).get("label"));
        assertEquals("gym", result.get(1).get("id"));
        assertEquals("Gym", result.get(1).get("label"));
        assertEquals("spa", result.get(2).get("id"));
        assertEquals("Spa", result.get(2).get("label"));
    }

    @Test
    @DisplayName("Should get rating range from database")
    void getRatingRange_shouldReturnRatingRange() {
        // Mock JdbcTemplate response
        Map<String, Object> dbResult = new HashMap<>();
        dbResult.put("min_rating", 3.2);
        dbResult.put("max_rating", 4.7);
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(dbResult);

        // Execute method
        Map<String, Double> result = filterOptionsService.getRatingRange();

        // Verify
        assertEquals(3.2, result.get("min"));
        assertEquals(4.7, result.get("max"));
        assertEquals(0.5, result.get("step"));
    }

    @Test
    @DisplayName("Should update filters with dynamic data")
    void updateFiltersWithDynamicData_shouldUpdateAllSections() {
        // Setup test data
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        filters.put("position", "left");
        filters.put("sections", new ArrayList<>());

        // Mock required methods
        doReturn(Arrays.asList("STANDARD", "DELUXE")).when(filterOptionsService).getDistinctRoomTypes();
        
        // Use HashMap instead of Map.of() for mutable maps
        Map<String, Double> priceRange = new HashMap<>();
        priceRange.put("min", 100.0);
        priceRange.put("max", 500.0);
        priceRange.put("step", 10.0);
        doReturn(priceRange).when(filterOptionsService).getPriceRange();
        
        List<Map<String, String>> amenities = new ArrayList<>();
        Map<String, String> pool = new HashMap<>();
        pool.put("id", "pool");
        pool.put("label", "Pool");
        Map<String, String> gym = new HashMap<>();
        gym.put("id", "gym");
        gym.put("label", "Gym");
        amenities.add(pool);
        amenities.add(gym);
        doReturn(amenities).when(filterOptionsService).getAllAmenities();
        
        Map<String, Double> ratingRange = new HashMap<>();
        ratingRange.put("min", 3.0);
        ratingRange.put("max", 5.0);
        ratingRange.put("step", 0.5);
        doReturn(ratingRange).when(filterOptionsService).getRatingRange();

        // Execute method
        Map<String, Object> result = filterOptionsService.updateFiltersWithDynamicData(filters);

        // Verify
        assertTrue(result.containsKey("sections"));
        List<Map<String, Object>> sections = (List<Map<String, Object>>) result.get("sections");
        
        // Should have 4 sections (price, amenities, room_type, rating)
        assertEquals(4, sections.size());
        
        // Verify each section was created properly
        boolean priceFound = false;
        boolean amenitiesFound = false;
        boolean roomTypeFound = false;
        boolean ratingFound = false;
        
        for (Map<String, Object> section : sections) {
            String id = (String) section.get("id");
            
            if ("price".equals(id)) {
                priceFound = true;
                Map<String, Object> options = (Map<String, Object>) section.get("options");
                assertEquals(100.0, options.get("min"));
                assertEquals(500.0, options.get("max"));
            } else if ("amenities".equals(id)) {
                amenitiesFound = true;
                List<Map<String, String>> options = (List<Map<String, String>>) section.get("options");
                assertEquals(2, options.size());
            } else if ("room_type".equals(id)) {
                roomTypeFound = true;
                List<Map<String, String>> options = (List<Map<String, String>>) section.get("options");
                assertEquals(2, options.size());
            } else if ("rating".equals(id)) {
                ratingFound = true;
                Map<String, Object> options = (Map<String, Object>) section.get("options");
                assertEquals(3.0, options.get("min"));
                assertEquals(5.0, options.get("max"));
            }
        }
        
        assertTrue(priceFound);
        assertTrue(amenitiesFound);
        assertTrue(roomTypeFound);
        assertTrue(ratingFound);
    }

    @Test
    @DisplayName("Should create new filters map if input is null")
    void updateFiltersWithDynamicData_whenFiltersNull_shouldCreateNewFiltersMap() {
        // Mock required methods
        doReturn(Arrays.asList()).when(filterOptionsService).getDistinctRoomTypes();
        
        // Use HashMap instead of Map.of()
        Map<String, Double> priceRange = new HashMap<>();
        priceRange.put("min", 0.0);
        priceRange.put("max", 1000.0);
        priceRange.put("step", 10.0);
        doReturn(priceRange).when(filterOptionsService).getPriceRange();
        
        doReturn(new ArrayList<>()).when(filterOptionsService).getAllAmenities();
        
        Map<String, Double> ratingRange = new HashMap<>();
        ratingRange.put("min", 1.0);
        ratingRange.put("max", 5.0);
        ratingRange.put("step", 0.5);
        doReturn(ratingRange).when(filterOptionsService).getRatingRange();

        // Execute method
        Map<String, Object> result = filterOptionsService.updateFiltersWithDynamicData(null);

        // Verify
        assertNotNull(result);
        assertTrue((Boolean) result.get("enabled"));
        assertEquals("left", result.get("position"));
        assertTrue(result.containsKey("sections"));
    }
} 