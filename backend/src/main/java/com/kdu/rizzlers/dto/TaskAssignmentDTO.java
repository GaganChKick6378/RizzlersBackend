package com.kdu.rizzlers.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for task assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentDTO {
    
    private Integer taskId;
    private String externalRoomId;
    private String roomNumber;
    private Integer staffId;
    private String staffName;
    private LocalTime startTime;
    private String taskTypeName;    // Service-level type (internal logic)
    private String dbTaskTypeName;  // Database-level type mapping
    private Duration duration;
    private LocalDate date;
} 