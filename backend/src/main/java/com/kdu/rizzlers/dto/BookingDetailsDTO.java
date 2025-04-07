package com.kdu.rizzlers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailsDTO {
    private Integer bookingId;
    private Integer propertyId;
    private String propertyName;
    private ZonedDateTime checkInDate;
    private ZonedDateTime checkOutDate;
    private Integer guestId;
    private String guestName;
    private String guestEmail;
} 