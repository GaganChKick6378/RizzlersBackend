package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * DTO for detailed booking confirmation information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmationDetailsResponse {
    
    private Boolean success;
    private String message;
    private BookingDetails bookingDetails;
    private RoomTotalSummary roomTotalSummary;
    private GuestInformation guestInformation;
    private BillingAddress billingAddress;
    private PaymentInformation paymentInformation;
    
    /**
     * Section 1: Booking details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingDetails {
        private Integer bookingId;
        private Integer roomTypeId;
        private String roomTypeName;
        private String roomImage;
        private Integer adultCount;
        private Integer childCount;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer promotionId;
        private String promotionTitle;
        private String promotionDescription;
        private BigDecimal averageNightlyPrice;
        private String status;
        private Integer statusId;
    }
    
    /**
     * Section 2: Room total summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomTotalSummary {
        private BigDecimal nightlyRate;
        private BigDecimal subtotal;
        private BigDecimal taxesAndFees;
        private BigDecimal totalForStay;
    }
    
    /**
     * Section 3: Guest information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestInformation {
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
    }
    
    /**
     * Section 4: Billing address
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingAddress {
        private String firstName;
        private String lastName;
        private String mailingAddress1;
        private String mailingAddress2;
        private String country;
        private String city;
        private String state;
        private String zip;
        private String phone;
        private String email;
    }
    
    /**
     * Section 5: Payment information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInformation {
        private String maskedCardNumber;
        private String expMonth;
        private String expYear;
        private Boolean specialOffers;
        private Boolean agreedToTerms;
    }
} 