package com.kdu.rizzlers.dto.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * Request DTO for checking room availability status and pricing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityStatusRequestDTO {
    private Integer propertyId;
    private Integer roomTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer guests; // Total guests (optional, will be calculated from guestCount if not provided)
    private Map<String, Integer> guestCount; // Breakdown of guests by type (Adults, Seniors, Kids, Teens)
    private Integer roomCount;
    private Integer bedCount;
    private Integer promotionId; // Promotion ID with suffix (001 for GraphQL, 002 for RDS)
} 