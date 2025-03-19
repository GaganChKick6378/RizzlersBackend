package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeImageRequest {

    @NotNull(message = "Tenant ID is required")
    private Integer tenantId;

    @NotNull(message = "Room type ID is required")
    private Integer roomTypeId;

    @NotNull(message = "Property ID is required")
    private Integer propertyId;

    @NotEmpty(message = "At least one image URL is required")
    private String[] imageUrls;

    private Integer displayOrder = 0;
} 