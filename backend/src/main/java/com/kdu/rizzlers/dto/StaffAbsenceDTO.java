package com.kdu.rizzlers.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for staff absence requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAbsenceDTO {
    
    @NotNull(message = "Staff ID is required")
    private Integer staffId;
    
    @NotNull(message = "Absence date is required")
    private LocalDate date;
}