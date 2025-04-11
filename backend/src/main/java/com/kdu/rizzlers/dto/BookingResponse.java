package com.kdu.rizzlers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for booking response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Integer bookingId;
    
    private Integer propertyId;
    
    private String propertyName;
    
    private Integer roomTypeId;
    
    private String roomTypeName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    
    private Integer adultCount;
    
    private Integer childCount;
    
    private BigDecimal totalCost;
    
    private BigDecimal amountDueAtResort;
    
    private String guestName;
    
    private Integer guestId;
    
    private String status;
    
    private Integer promotionId;
    
    private String promotionName;
    
    private String confirmationCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime bookingTime;
    
    // Fields for multiple room bookings
    private List<Integer> allBookingIds;
    
    private List<Integer> bookedRoomIds;
    
    private Integer totalRoomsBooked;
    
    @Builder.Default
    private Boolean success = true;
    
    private String message;
    
    /**
     * Create a failure response
     * 
     * @param message the error message
     * @return a BookingResponse with success=false
     */
    public static BookingResponse failure(String message) {
        return BookingResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 