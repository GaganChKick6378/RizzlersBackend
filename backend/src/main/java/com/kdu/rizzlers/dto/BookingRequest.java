package com.kdu.rizzlers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for room booking request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Property ID is required")
    private Integer propertyId;

    @NotNull(message = "Room type ID is required")
    private Integer roomTypeId;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least one guest is required")
    private Integer guests;

    @NotNull(message = "Guest count details are required")
    private Map<String, Integer> guestCount;

    @NotNull(message = "Room count is required")
    @Min(value = 1, message = "At least one room is required")
    private Integer roomCount;

    @NotNull(message = "Bed count is required")
    @Min(value = 1, message = "At least one bed is required")
    private Integer bedCount;

    private Integer promotionId;

    @NotNull(message = "Travel information is required")
    @Valid
    private TravelInfo travelInfo;

    @NotNull(message = "Billing information is required")
    @Valid
    private BillingInfo billingInfo;

    @NotNull(message = "Payment information is required")
    @Valid
    private PaymentInfo paymentInfo;

    /**
     * Guest travel information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelInfo {
        @NotBlank(message = "First name is required")
        private String first_name;

        @NotBlank(message = "Last name is required")
        private String last_name;

        @NotBlank(message = "Phone number is required")
        private String phone;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    /**
     * Billing information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingInfo {
        @NotBlank(message = "First name is required")
        private String first_name;

        @NotBlank(message = "Last name is required")
        private String last_name;

        @NotBlank(message = "Mailing address is required")
        private String mailing_address1;

        private String mailing_address2;

        @NotBlank(message = "Country is required")
        private String country;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State/Province is required")
        private String state;

        @NotBlank(message = "ZIP/Postal code is required")
        private String zip;

        @NotBlank(message = "Phone number is required")
        private String phone;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    /**
     * Payment information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        @NotBlank(message = "Card number is required")
        private String card_number;

        @NotBlank(message = "Expiration month is required")
        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expiration month must be between 01 and 12")
        private String exp_mm;

        @NotBlank(message = "Expiration year is required")
        @Pattern(regexp = "^20\\d{2}$", message = "Expiration year must be a 4-digit year")
        private String exp_yy;

        private Boolean special_offers;

        @NotNull(message = "Agreement to terms is required")
        @AssertTrue(message = "You must agree to the terms and conditions")
        private Boolean agreed_to_terms;
    }
} 