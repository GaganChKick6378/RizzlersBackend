package com.kdu.rizzlers.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ReviewRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Get review data for a room type
     */
    public Optional<Map<String, Object>> findReviewByRoomTypeId(Integer roomTypeId) {
        String sql = "SELECT rating, description, images, review_count FROM reviews WHERE room_type_id = ?";
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, roomTypeId);
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(results.get(0));
    }
    
    /**
     * Get amenities for a room type
     */
    public List<String> findAmenitiesByRoomTypeId(Integer roomTypeId) {
        String sql = "SELECT a.name FROM amenities a " +
                     "JOIN room_type_amenities rta ON a.id = rta.amenity_id " +
                     "WHERE rta.room_type_id = ?";
        
        return jdbcTemplate.queryForList(sql, String.class, roomTypeId);
    }
    
    /**
     * Parse a PostgreSQL text array into a List of Strings
     */
    public List<String> parseTextArray(String textArray) {
        if (textArray == null || textArray.isEmpty() || textArray.equals("{}")) {
            return List.of();
        }
        
        // Remove the curly braces and split by comma
        String content = textArray.substring(1, textArray.length() - 1);
        return Arrays.stream(content.split(","))
                .map(s -> s.trim().replace("\"", ""))
                .collect(Collectors.toList());
    }
    
    /**
     * Get the first image for a room type from reviews
     * 
     * @param roomTypeId the room type ID
     * @return the first image URL or null if none found
     */
    public String findFirstImageForRoomType(Integer roomTypeId) {
        String sql = "SELECT images FROM reviews WHERE room_type_id = ? LIMIT 1";
        
        List<String> results = jdbcTemplate.queryForList(sql, String.class, roomTypeId);
        
        if (results.isEmpty() || results.get(0) == null) {
            return null;
        }
        
        String imagesJson = results.get(0);
        List<String> images = parseTextArray(imagesJson);
        
        return images.isEmpty() ? null : images.get(0);
    }
    
    /**
     * Updates the rating of a room in the reviews table using a moving average approach
     * when a new review is submitted
     * 
     * @param roomTypeId the room type ID
     * @param newRating the new rating from the user review
     * @return true if update was successful, false otherwise
     */
    public boolean updateRoomRating(Integer roomTypeId, Integer newRating) {
        try {
            log.info("Updating room rating for roomTypeId: {}, new rating: {}", roomTypeId, newRating);
            
            // First, get the current rating and review count
            String selectSql = "SELECT rating, review_count FROM reviews WHERE room_type_id = ?";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSql, roomTypeId);
            
            if (results.isEmpty()) {
                // If no record exists, create a new entry with this rating as the initial value
                log.info("No existing rating found for roomTypeId: {}. Creating new entry.", roomTypeId);
                String insertSql = "INSERT INTO reviews (room_type_id, rating, review_count, description, images) VALUES (?, ?, 1, '', '{}')";
                jdbcTemplate.update(insertSql, roomTypeId, newRating);
                return true;
            }
            
            // Calculate the new rating using moving average
            Map<String, Object> currentData = results.get(0);
            double currentRating = currentData.get("rating") != null ? ((Number) currentData.get("rating")).doubleValue() : 0.0;
            int reviewCount = currentData.get("review_count") != null ? ((Number) currentData.get("review_count")).intValue() : 0;
            
            log.info("Current rating for roomTypeId {}: {}, review count: {}", roomTypeId, currentRating, reviewCount);
            
            // Moving average calculation: newAvg = ((oldAvg * oldCount) + newValue) / (oldCount + 1)
            double updatedRating;
            if (reviewCount == 0) {
                updatedRating = newRating;
            } else {
                updatedRating = ((currentRating * reviewCount) + newRating) / (reviewCount + 1);
            }
            
            log.info("Updating rating for roomTypeId {}: {} -> {} (review count: {} -> {})", 
                    roomTypeId, currentRating, updatedRating, reviewCount, reviewCount + 1);
            
            // Update the reviews table with new rating and increment review count
            String updateSql = "UPDATE reviews SET rating = ?, review_count = ? WHERE room_type_id = ?";
            jdbcTemplate.update(updateSql, updatedRating, reviewCount + 1, roomTypeId);
            
            return true;
        } catch (Exception e) {
            log.error("Error updating room rating for roomTypeId: {}: {}", roomTypeId, e.getMessage(), e);
            return false;
        }
    }
} 