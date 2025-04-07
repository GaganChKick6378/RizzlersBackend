package com.kdu.rizzlers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for response from review token validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewValidationResponseDTO {
    
    private boolean valid;
    private String message;
    private Integer bookingId;
    private Integer guestId;
    private Integer propertyId;
    private Integer roomTypeId;
    private String propertyName;
    private String guestName;
} 