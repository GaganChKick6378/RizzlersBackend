package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableRoomDTO {
    private Integer roomTypeId;
    private Integer roomId;
    private String roomTypeName;
    private Integer maxCapacity;
    private Integer areaInSquareFeet;
    private Integer singleBed;
    private Integer doubleBed;
    private String propertyAddress;
    private Double price; // minimum nightly rate
    
    // List of available room IDs for this room type
    private List<Integer> availableRoomIds;
    
    // Count of available rooms
    private Integer availableRoomCount;
    
    // New fields combining GraphQL and RDS data
    private List<String> roomImages;
    private String roomDescription;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rating {
        private Double stars;
        private Integer reviewCount;
    }
    
    private Rating rating;
    private String landmark;
    private List<String> bedTypes;
    private List<String> amenities;
} 