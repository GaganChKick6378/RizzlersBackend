package com.kdu.rizzlers.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 * This exception is typically thrown when attempting to retrieve, update, or delete
 * a resource that does not exist in the system.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with no detailed message.
     */
    public ResourceNotFoundException() {
        super("Resource not found");
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException for a specific resource type and identifier.
     *
     * @param resourceName the name of the resource type (e.g., "Student", "Course")
     * @param fieldName the name of the field used to identify the resource (e.g., "id", "email")
     * @param fieldValue the value of the identifier field
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 