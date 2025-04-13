package com.kdu.rizzlers.dto;

import com.kdu.rizzlers.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    
    private Integer userId;
    private String username;
    private String email;
    private UserRole role;
    private Integer staffId;
    private String staffName;
    private Boolean isActive;
} 