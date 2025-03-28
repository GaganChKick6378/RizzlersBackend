package com.kdu.rizzlers.service;

import java.util.List;
import java.util.Map;

/**
 * Service for gathering and providing filter options for the results page
 */
public interface FilterOptionsService {
    
    /**
     * Get distinct room types from GraphQL
     * 
     * @return List of room type names
     */
    List<String> getDistinctRoomTypes();
    
    /**
     * Get price range for room rates from GraphQL
     * 
     * @return Map containing min and max prices
     */
    Map<String, Integer> getPriceRange();
    
    /**
     * Get all available amenities from database
     * 
     * @return List of maps with id and label for each amenity
     */
    List<Map<String, String>> getAllAmenities();
    
    /**
     * Get available rating range from database
     * 
     * @return Map containing min and max ratings
     */
    Map<String, Double> getRatingRange();
    
    /**
     * Update results page filters with dynamically gathered data
     * 
     * @param filters The current filters map to update
     * @return Updated filters map
     */
    Map<String, Object> updateFiltersWithDynamicData(Map<String, Object> filters);
} 