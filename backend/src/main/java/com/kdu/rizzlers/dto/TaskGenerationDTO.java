package com.kdu.rizzlers.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for task generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskGenerationDTO {
    
    private String externalRoomId;
    private String roomNumber;
    private LocalTime windowStart;
    private LocalTime windowEnd;
    private String taskTypeName;     // Service-level type (internal logic)
    private String dbTaskTypeName;   // Database-level type mapping
    private int priority;
    private LocalDate date;
} 