package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageConfigResponse {
    private Integer tenantId;
    private String page;
    
    // Logo and UI elements
    @JsonProperty("header_logo")
    private Map<String, Object> headerLogo;
    
    @JsonProperty("page_title")
    private Map<String, Object> pageTitle;
    
    @JsonProperty("banner_image")
    private Map<String, Object> bannerImage;
    
    // Footer configuration
    @JsonProperty("footer")
    private Map<String, Object> footer;
    
    /**
     * Languages configuration with available language options
     * 
     * Example JSON structure:
     * {
     *   "options": [
     *     {"code": "EN", "name": "English", "active": true},
     *     {"code": "ES", "name": "Español", "active": true},
     *     {"code": "FR", "name": "Français", "active": true}
     *   ],
     *   "default": "EN"
     * }
     */
    @JsonProperty("languages")
    private Map<String, Object> languages;
    
    /**
     * Currencies configuration with available currency options
     * 
     * Example JSON structure:
     * {
     *   "options": [
     *     {"code": "USD", "symbol": "$", "name": "US Dollar", "active": true},
     *     {"code": "EUR", "symbol": "€", "name": "Euro", "active": true},
     *     {"code": "GBP", "symbol": "£", "name": "British Pound", "active": true}
     *   ],
     *   "default": "USD"
     * }
     */
    @JsonProperty("currencies")
    private Map<String, Object> currencies;
    
    // Stay configuration
    @JsonProperty("length_of_stay")
    private Map<String, Object> lengthOfStay;
    
    // Guest options
    @JsonProperty("guest_options")
    private Map<String, Object> guestOptions;
    
    // Room options
    @JsonProperty("room_options")
    private Map<String, Object> roomOptions;
    
    // Accessibility options
    @JsonProperty("accessibility_options")
    private Map<String, Object> accessibilityOptions;
    
    // Number of rooms
    @JsonProperty("number_of_rooms")
    private Map<String, Object> numberOfRooms;
    
    // Guest type definitions
    @JsonProperty("guest_types")
    private List<GuestTypeDefinitionResponse> guestTypes;
    
    // Properties assigned to the tenant
    @JsonProperty("properties")
    private List<TenantPropertyAssignmentResponse> properties;
} 