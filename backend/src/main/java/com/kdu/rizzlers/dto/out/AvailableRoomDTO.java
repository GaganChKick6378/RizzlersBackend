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
    
    // Total price for the requested number of rooms, not per room price
    private Double price;
    
    // Number of rooms that were requested and included in the price calculation
    // (Only room types with at least this many available rooms are returned)
    private Integer roomCount;
    
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