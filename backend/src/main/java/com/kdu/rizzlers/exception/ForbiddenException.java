package com.kdu.rizzlers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user is authenticated but not authorized to access a resource
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
} 