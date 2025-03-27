package com.kdu.rizzlers.constants;

public final class AppConstants {
    
    // Application Constants
    public static final String API_BASE_PATH = "/api";
    public static final String HEALTH_CHECK_PATH = "/health";
    
    // Pagination Default Values
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    
    // Date Format Constants
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    // Error Messages
    public static final String RESOURCE_NOT_FOUND = "%s not found with %s : '%s'";
    public static final String INVALID_REQUEST = "Invalid request";
    
    private AppConstants() {
        // Private constructor to prevent instantiation
    }
} 