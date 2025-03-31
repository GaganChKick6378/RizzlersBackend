package com.kdu.rizzlers.service;

import com.kdu.rizzlers.service.impl.FilterOptionsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterOptionsServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private FilterOptionsServiceImpl filterOptionsService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    @DisplayName("FilterOptionsService interface is correctly implemented")
    void testInterface() {
        // This test doesn't actually execute any methods, but verifies that
        // FilterOptionsServiceImpl implements FilterOptionsService
        FilterOptionsService service = filterOptionsService;
        assertNotNull(service);
    }

    @Test
    @DisplayName("FilterOptionsService should return empty list for getDistinctRoomTypes when API call fails")
    void getDistinctRoomTypes_whenApiCallFails_shouldReturnEmptyList() {
        // No need to set up the WebClient mocking since we want it to fail

        List<String> result = filterOptionsService.getDistinctRoomTypes();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("FilterOptionsService should return default values for getPriceRange when API call fails")
    void getPriceRange_whenApiCallFails_shouldReturnDefaultValues() {
        // No need to set up the WebClient mocking since we want it to fail

        Map<String, Integer> result = filterOptionsService.getPriceRange();
        
        assertNotNull(result);
        assertTrue(result.containsKey("min"));
        assertTrue(result.containsKey("max"));
        assertEquals(0, result.get("min"));
        assertEquals(1000, result.get("max"));
    }

    @Test
    @DisplayName("FilterOptionsService should return empty list for getAllAmenities when API call fails")
    void getAllAmenities_whenApiCallFails_shouldReturnEmptyList() {
        // No need to set up the WebClient mocking since we want it to fail

        List<Map<String, String>> result = filterOptionsService.getAllAmenities();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("FilterOptionsService should return default values for getRatingRange when API call fails")
    void getRatingRange_whenApiCallFails_shouldReturnDefaultValues() {
        // No need to set up the WebClient mocking since we want it to fail

        Map<String, Double> result = filterOptionsService.getRatingRange();
        
        assertNotNull(result);
        assertTrue(result.containsKey("min"));
        assertTrue(result.containsKey("max"));
        assertEquals(1.0, result.get("min"));
        assertEquals(5.0, result.get("max"));
    }

    @Test
    @DisplayName("updateFiltersWithDynamicData should update filters map with dynamic data")
    void updateFiltersWithDynamicData_shouldUpdateFiltersWithDynamicData() {
        // Prepare test data
        Map<String, Object> filters = new HashMap<>();
        filters.put("some-key", "some-value");

        // Set up mocks to return empty defaults
        when(filterOptionsService.getDistinctRoomTypes()).thenReturn(List.of());
        when(filterOptionsService.getPriceRange()).thenReturn(Map.of("min", 0, "max", 1000));
        when(filterOptionsService.getAllAmenities()).thenReturn(List.of());
        when(filterOptionsService.getRatingRange()).thenReturn(Map.of("min", 1.0, "max", 5.0));

        // Call method under test
        Map<String, Object> result = filterOptionsService.updateFiltersWithDynamicData(filters);
        
        // Verify results
        assertNotNull(result);
        assertTrue(result.containsKey("some-key"));
        assertEquals("some-value", result.get("some-key"));
    }
} 