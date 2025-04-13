package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.BookingCancellationResponse;

public interface BookingCancellationService {
    /**
     * Request booking cancellation and send OTP to guest's email
     * @param guestId The ID of the guest
     * @param bookingId The ID of the booking to cancel
     * @return Response with status of the cancellation request
     */
    BookingCancellationResponse requestCancellation(Integer guestId, Integer bookingId);
    
    /**
     * Verify OTP and cancel booking if it matches
     * @param guestId The ID of the guest
     * @param bookingId The ID of the booking to cancel
     * @param otp The OTP to verify
     * @return Response with details of the cancelled booking
     */
    BookingCancellationResponse verifyCancellation(Integer guestId, Integer bookingId, String otp);
    
    /**
     * Cancel booking directly for authenticated users
     * @param guestId The ID of the guest
     * @param bookingId The ID of the booking to cancel
     * @param idToken The ID token from Cognito
     * @return Response with details of the cancelled booking
     */
    BookingCancellationResponse authenticatedCancellation(Integer guestId, Integer bookingId, String idToken);
} 