package com.kdu.rizzlers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Travel Itinerary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelItineraryDto {
    private String bookingId;
    private LocalDate bookingDate;
    private String travelerName;
    private String travelerEmail;
    private String travelerPhone;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String destination;
    private BigDecimal totalAmount;
    private BigDecimal promotionApplied;
    private BigDecimal amountPaid;
    private BigDecimal amountDue;
    private String paymentMethod;
    private String billingAddress;
} 