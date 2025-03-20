package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPropertyAssignmentRequest {

    @NotNull(message = "Tenant ID is required")
    private Integer tenantId;

    @NotNull(message = "Property ID is required")
    private Integer propertyId;

    private Boolean isAssigned = false;
} 