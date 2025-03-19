package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestTypeDefinitionRequest {

    @NotNull(message = "Tenant ID is required")
    private Integer tenantId;

    @NotBlank(message = "Guest type is required")
    @Size(max = 50, message = "Guest type cannot exceed 50 characters")
    private String guestType;

    @NotNull(message = "Minimum age is required")
    @Min(value = 0, message = "Minimum age cannot be negative")
    private Integer minAge;

    @NotNull(message = "Maximum age is required")
    @Min(value = 0, message = "Maximum age cannot be negative")
    private Integer maxAge;

    private String description;

    private Boolean isActive = true;
} 