package com.kdu.rizzlers.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
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
} 