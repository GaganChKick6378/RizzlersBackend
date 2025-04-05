package com.kdu.rizzlers.dto;

import com.kdu.rizzlers.entity.PropertyConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PropertyConfigurationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "Property ID is required")
        @Min(value = 1, message = "Property ID must be a positive number")
        private Integer propertyId;

        @NotBlank(message = "Contact number is required")
        private String contactNumber;

        @NotBlank(message = "Availability information is required")
        private String availability;

        @NotBlank(message = "Country is required")
        private String country;

        @NotNull(message = "Surcharge is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Surcharge must be a non-negative number")
        private BigDecimal surcharge;

        @NotNull(message = "Fees is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Fees must be a non-negative number")
        private BigDecimal fees;

        @NotBlank(message = "Terms and conditions are required")
        private String termsAndConditions;
        
        @DecimalMin(value = "0.0", inclusive = true, message = "Tax must be a non-negative number")
        private BigDecimal tax;

        public PropertyConfiguration toEntity() {
            return PropertyConfiguration.builder()
                    .propertyId(propertyId)
                    .contactNumber(contactNumber)
                    .availability(availability)
                    .country(country)
                    .surcharge(surcharge)
                    .fees(fees)
                    .termsAndConditions(termsAndConditions)
                    .tax(tax)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Integer propertyId;
        private String contactNumber;
        private String availability;
        private String country;
        private BigDecimal surcharge;
        private BigDecimal fees;
        private String termsAndConditions;
        private Boolean isActive;
        private BigDecimal tax;

        public static Response fromEntity(PropertyConfiguration entity) {
            return Response.builder()
                    .id(entity.getId())
                    .propertyId(entity.getPropertyId())
                    .contactNumber(entity.getContactNumber())
                    .availability(entity.getAvailability())
                    .country(entity.getCountry())
                    .surcharge(entity.getSurcharge())
                    .fees(entity.getFees())
                    .termsAndConditions(entity.getTermsAndConditions())
                    .isActive(entity.getIsActive())
                    .tax(entity.getTax())
                    .build();
        }
    }
} 