package com.kdu.rizzlers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a room is not available for booking
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RoomUnavailableException extends RuntimeException {
    
    public RoomUnavailableException(String message) {
        super(message);
    }
    
    public RoomUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
} 