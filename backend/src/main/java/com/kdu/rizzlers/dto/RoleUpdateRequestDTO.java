package com.kdu.rizzlers.dto;

import com.kdu.rizzlers.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for role update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequestDTO {
    
    @NotNull(message = "Role is required")
    private UserRole role;
} 