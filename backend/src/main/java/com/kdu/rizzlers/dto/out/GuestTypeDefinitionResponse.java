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
public class GuestTypeDefinitionResponse {
    private Long id;
    private Integer tenantId;
    private String guestType;
    private Integer minAge;
    private Integer maxAge;
    private String description;
    private Boolean isActive;
    private Integer maxCount;
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
} 