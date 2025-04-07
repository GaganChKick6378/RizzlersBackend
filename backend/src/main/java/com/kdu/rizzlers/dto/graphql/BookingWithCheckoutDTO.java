package com.kdu.rizzlers.dto.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for bookings with checkout information from GraphQL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingWithCheckoutDTO {
    
    @JsonProperty("booking_id")
    private Integer bookingId;
    
    @JsonProperty("check_out_date")
    private String checkOutDate;
    
    @JsonProperty("guest_id")
    private Integer guestId;
    
    @JsonProperty("booking_status")
    private BookingStatusDTO bookingStatus;
    
    @JsonProperty("property_id")
    private Integer propertyId;
    
    @JsonProperty("room_booked")
    private List<RoomBookedDTO> roomBooked;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingStatusDTO {
        private String status;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomBookedDTO {
        private RoomDTO room;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDTO {
        @JsonProperty("room_id")
        private Integer roomId;
        
        @JsonProperty("room_type_id")
        private Integer roomTypeId;
    }
} 