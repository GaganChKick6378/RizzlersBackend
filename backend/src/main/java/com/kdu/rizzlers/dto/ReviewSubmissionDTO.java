package com.kdu.rizzlers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for submitting a review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmissionDTO {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    private Integer propertyId;
    
    private Integer roomTypeId;
    
    @NotNull(message = "Cleanliness rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be greater than 5")
    private Integer cleanlinessRating;
    
    @NotNull(message = "Staff service rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be greater than 5")
    private Integer staffServiceRating;
    
    @NotNull(message = "Comfort rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be greater than 5")
    private Integer comfortRating;
    
    @NotNull(message = "Location rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be greater than 5")
    private Integer locationRating;
    
    @NotNull(message = "Value rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be greater than 5")
    private Integer valueRating;
    
    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be greater than 5")
    private Integer overallRating;
    
    private String comment;
    
    private String[] images;
} 