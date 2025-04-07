package com.kdu.rizzlers.exception;

/**
 * Exception thrown when an attempt is made to book a room with a date range
 * that overlaps with an existing booking for the same room.
 */
public class OverlappingBookingException extends RuntimeException {

    private Integer roomId;
    private String startDate;
    private String endDate;

    public OverlappingBookingException(String message) {
        super(message);
    }

    public OverlappingBookingException(String message, Integer roomId, String startDate, String endDate) {
        super(message);
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public OverlappingBookingException(String message, Throwable cause) {
        super(message, cause);
    }

    public Integer getRoomId() {
        return roomId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
} 