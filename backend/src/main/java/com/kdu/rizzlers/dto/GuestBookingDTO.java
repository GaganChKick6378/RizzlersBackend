package com.kdu.rizzlers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for representing a booking in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestBookingDTO {
    
    private Integer bookingId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private ZonedDateTime checkInDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private ZonedDateTime checkOutDate;
    
    private GuestDTO guest;
    
    private PropertyDTO propertyBooked;
    
    private Integer statusId;
    
    private String statusName;
    
    private Integer roomTypeId;
    
    private String roomImage;
    
    /**
     * DTO for guest information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestDTO {
        private String guestName;
        private Integer guestId;
    }
    
    /**
     * DTO for property information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyDTO {
        private String propertyName;
    }
} 