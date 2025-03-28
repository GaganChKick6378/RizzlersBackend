package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.repository.ReviewRepository;
import com.kdu.rizzlers.service.FilterOptionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilterOptionsServiceImpl implements FilterOptionsService {

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ReviewRepository reviewRepository;
    
    @Value("${graphql.endpoint}")
    private String graphqlEndpoint;
    
    @Value("${graphql.api-key}")
    private String apiKey;
    
    /**
     * Initialize WebClient for GraphQL calls
     * 
     * @return Configured WebClient
     */
    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(graphqlEndpoint)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }
    
    @Override
    public List<String> getDistinctRoomTypes() {
        String query = "query ListRoomTypes { listRoomTypes { room_type_name } }";
        
        try {
            String response = getWebClient().post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("query", query))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response == null) {
                log.error("Null response from GraphQL for room types");
                return Collections.emptyList();
            }
            
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.get("data");
            
            if (dataNode == null || dataNode.isNull()) {
                log.error("No data found in response for room types: {}", response);
                return Collections.emptyList();
            }
            
            JsonNode roomTypesNode = dataNode.get("listRoomTypes");
            if (roomTypesNode == null || !roomTypesNode.isArray()) {
                log.error("Invalid listRoomTypes data: {}", dataNode);
                return Collections.emptyList();
            }
            
            // Extract and de-duplicate room type names
            Set<String> distinctRoomTypes = new HashSet<>();
            for (JsonNode roomType : roomTypesNode) {
                if (roomType.has("room_type_name")) {
                    distinctRoomTypes.add(roomType.get("room_type_name").asText());
                }
            }
            
            return new ArrayList<>(distinctRoomTypes);
        } catch (Exception e) {
            log.error("Error fetching room types from GraphQL", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Map<String, Integer> getPriceRange() {
        String query = "query ListRoomRates { listRoomRates(take: 200000) { basic_nightly_rate } }";
        
        try {
            String response = getWebClient().post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("query", query))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response == null) {
                log.error("Null response from GraphQL for room rates");
                return defaultPriceRange();
            }
            
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.get("data");
            
            if (dataNode == null || dataNode.isNull()) {
                log.error("No data found in response for room rates: {}", response);
                return defaultPriceRange();
            }
            
            JsonNode ratesNode = dataNode.get("listRoomRates");
            if (ratesNode == null || !ratesNode.isArray() || ratesNode.size() == 0) {
                log.error("Invalid listRoomRates data: {}", dataNode);
                return defaultPriceRange();
            }
            
            // Find min and max prices
            int minPrice = Integer.MAX_VALUE;
            int maxPrice = Integer.MIN_VALUE;
            
            for (JsonNode rate : ratesNode) {
                if (rate.has("basic_nightly_rate")) {
                    int price = rate.get("basic_nightly_rate").asInt();
                    minPrice = Math.min(minPrice, price);
                    maxPrice = Math.max(maxPrice, price);
                }
            }
            
            // Ensure we found at least one valid price
            if (minPrice == Integer.MAX_VALUE || maxPrice == Integer.MIN_VALUE) {
                return defaultPriceRange();
            }
            
            // Round min down to nearest 10, max up to nearest 10 for better UX
            minPrice = (minPrice / 10) * 10;
            maxPrice = ((maxPrice + 9) / 10) * 10;
            
            return Map.of("min", minPrice, "max", maxPrice, "step", 10);
        } catch (Exception e) {
            log.error("Error fetching price range from GraphQL", e);
            return defaultPriceRange();
        }
    }
    
    private Map<String, Integer> defaultPriceRange() {
        return Map.of("min", 0, "max", 1000, "step", 10);
    }
    
    @Override
    public List<Map<String, String>> getAllAmenities() {
        try {
            String sql = "SELECT DISTINCT name FROM amenities ORDER BY name";
            List<String> amenityNames = jdbcTemplate.queryForList(sql, String.class);
            
            return amenityNames.stream()
                .map(name -> {
                    String id = name.toLowerCase().replace(' ', '_').replace('-', '_');
                    return Map.of("id", id, "label", name);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching amenities from database", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Map<String, Double> getRatingRange() {
        try {
            String sql = "SELECT MIN(rating) as min_rating, MAX(rating) as max_rating FROM reviews";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            
            double minRating = ((Number)result.get("min_rating")).doubleValue();
            double maxRating = ((Number)result.get("max_rating")).doubleValue();
            
            // Round to 1 decimal place for better UX
            minRating = Math.floor(minRating * 10) / 10;
            maxRating = Math.ceil(maxRating * 10) / 10;
            
            return Map.of("min", minRating, "max", maxRating, "step", 0.5);
        } catch (Exception e) {
            log.error("Error fetching rating range from database", e);
            return Map.of("min", 1.0, "max", 5.0, "step", 0.5);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> updateFiltersWithDynamicData(Map<String, Object> filters) {
        if (filters == null) {
            filters = new HashMap<>();
            filters.put("enabled", true);
            filters.put("position", "left");
        }
        
        // Create sections if they don't exist
        if (!filters.containsKey("sections")) {
            filters.put("sections", new ArrayList<>());
        }
        
        List<Map<String, Object>> sections = (List<Map<String, Object>>) filters.get("sections");
        
        // Update or add price filter
        updatePriceFilterSection(sections);
        
        // Update or add amenities filter
        updateAmenitiesFilterSection(sections);
        
        // Update or add room type filter
        updateRoomTypeFilterSection(sections);
        
        // Update or add rating filter
        updateRatingFilterSection(sections);
        
        return filters;
    }
    
    @SuppressWarnings("unchecked")
    private void updatePriceFilterSection(List<Map<String, Object>> sections) {
        // Find existing price filter section, or create new one
        Map<String, Object> priceSection = findOrCreateSection(sections, "price", "Price Range", "range");
        
        // Update options with dynamic price range
        Map<String, Integer> priceRange = getPriceRange();
        Map<String, Object> options = (Map<String, Object>) priceSection.get("options");
        if (options == null) {
            options = new HashMap<>();
            priceSection.put("options", options);
        }
        
        options.put("min", priceRange.get("min"));
        options.put("max", priceRange.get("max"));
        options.put("step", priceRange.get("step"));
    }
    
    @SuppressWarnings("unchecked")
    private void updateAmenitiesFilterSection(List<Map<String, Object>> sections) {
        // Find existing amenities filter section, or create new one
        Map<String, Object> amenitiesSection = findOrCreateSection(sections, "amenities", "Amenities", "checkbox");
        
        // Update options with dynamic amenities
        List<Map<String, String>> amenities = getAllAmenities();
        if (!amenities.isEmpty()) {
            amenitiesSection.put("options", amenities);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateRoomTypeFilterSection(List<Map<String, Object>> sections) {
        // Find existing room type filter section, or create new one
        Map<String, Object> roomTypeSection = findOrCreateSection(sections, "room_type", "Room Type", "checkbox");
        
        // Update options with dynamic room types
        List<String> roomTypes = getDistinctRoomTypes();
        List<Map<String, String>> roomTypeOptions = roomTypes.stream()
            .map(type -> {
                String id = type.toLowerCase();
                String label = type.replace('_', ' ');
                return Map.of("id", id, "label", label);
            })
            .collect(Collectors.toList());
        
        if (!roomTypeOptions.isEmpty()) {
            roomTypeSection.put("options", roomTypeOptions);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateRatingFilterSection(List<Map<String, Object>> sections) {
        // Find existing rating filter section, or create new one
        Map<String, Object> ratingSection = findOrCreateSection(sections, "rating", "Rating", "range");
        
        // Update options with dynamic rating range
        Map<String, Double> ratingRange = getRatingRange();
        Map<String, Object> options = (Map<String, Object>) ratingSection.get("options");
        if (options == null) {
            options = new HashMap<>();
            ratingSection.put("options", options);
        }
        
        options.put("min", ratingRange.get("min"));
        options.put("max", ratingRange.get("max"));
        options.put("step", ratingRange.get("step"));
    }
    
    private Map<String, Object> findOrCreateSection(List<Map<String, Object>> sections, String id, String label, String type) {
        // Try to find existing section
        Optional<Map<String, Object>> existingSection = sections.stream()
            .filter(section -> id.equals(section.get("id")))
            .findFirst();
        
        if (existingSection.isPresent()) {
            return existingSection.get();
        }
        
        // Create new section
        Map<String, Object> newSection = new HashMap<>();
        newSection.put("id", id);
        newSection.put("label", label);
        newSection.put("type", type);
        newSection.put("enabled", true);
        
        // Add to sections list
        sections.add(newSection);
        return newSection;
    }
} 