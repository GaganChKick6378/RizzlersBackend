package com.kdu.rizzlers.dto.graphql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for the response from the bookings list query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingsResponseDTO {
    private List<BookingWithCheckoutDTO> listBookings;
} 