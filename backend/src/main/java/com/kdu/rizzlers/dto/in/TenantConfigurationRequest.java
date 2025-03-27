package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfigurationRequest {

    @NotNull(message = "Tenant ID is required")
    private Integer tenantId;

    @NotBlank(message = "Page is required")
    @Pattern(regexp = "^(landing|results|details|checkout)$", message = "Page must be one of: landing, results, details, checkout")
    private String page;

    @NotBlank(message = "Field is required")
    @Size(max = 100, message = "Field name cannot exceed 100 characters")
    private String field;

    @NotBlank(message = "Value is required")
    private String value; // JSON string

    private Boolean isActive = true;
} 