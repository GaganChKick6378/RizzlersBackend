package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPropertyAssignmentResponse {
    private Long id;
    private Integer tenantId;
    private Integer propertyId;
    private Boolean isAssigned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}