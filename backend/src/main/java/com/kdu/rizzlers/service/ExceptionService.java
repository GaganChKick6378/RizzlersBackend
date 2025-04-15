package com.kdu.rizzlers.service;

import com.kdu.rizzlers.exception.BadRequestException;
import com.kdu.rizzlers.exception.ForbiddenException;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

/**
 * Service for creating and throwing exceptions with appropriate messages
 */
@Service
public class ExceptionService {

    /**
     * Throw a ResourceNotFoundException
     */
    public void throwResourceNotFound(String resourceName, String fieldName, Object fieldValue) {
        throw new ResourceNotFoundException(resourceName, fieldName, fieldValue);
    }
    
    /**
     * Throw a ResourceNotFoundException with a custom message
     */
    public void throwResourceNotFound(String message) {
        throw new ResourceNotFoundException(message);
    }
    
    /**
     * Throw a BadRequestException
     */
    public void throwBadRequest(String message) {
        throw new BadRequestException(message);
    }
    
    /**
     * Throw an UnauthorizedException
     */
    public void throwUnauthorized(String message) {
        throw new UnauthorizedException(message);
    }
    
    /**
     * Throw a ForbiddenException
     */
    public void throwForbidden(String message) {
        throw new ForbiddenException(message);
    }
    
    /**
     * Validate that an object exists or throw ResourceNotFoundException
     */
    public <T> T validateExists(T object, String resourceName, String fieldName, Object fieldValue) {
        if (object == null) {
            throwResourceNotFound(resourceName, fieldName, fieldValue);
        }
        return object;
    }
    
    /**
     * Validate a condition or throw BadRequestException
     */
    public void validateRequest(boolean condition, String message) {
        if (!condition) {
            throwBadRequest(message);
        }
    }
    
    /**
     * Validate authorization or throw ForbiddenException
     */
    public void validateAuthorization(boolean condition, String message) {
        if (!condition) {
            throwForbidden(message);
        }
    }
} 