package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonIgnore
    private Integer tenantId;
    
    @JsonProperty("guestType")
    private String guestType;
    
    @JsonProperty("minAge")
    private Integer minAge;
    
    @JsonProperty("maxAge")
    private Integer maxAge;
    
    private String description;
    private Boolean isActive;
    private Integer maxCount;
    
    @JsonIgnore
    private LocalDateTime createdAt;
    
    @JsonIgnore
    private LocalDateTime updatedAt;
} 