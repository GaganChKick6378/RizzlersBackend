package com.kdu.rizzlers.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Validator for tenant configuration fields.
 * Separating validation logic to keep service implementation clean.
 */
@Slf4j
@Component
public class ConfigurationValidator {

    /**
     * Validates header logo configuration
     */
    public boolean validateHeaderLogo(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("url")) {
            log.warn("Header logo missing required 'url' field");
            return false;
        }
        if (!valueMap.containsKey("alt")) {
            log.warn("Header logo missing 'alt' field, adding default");
            valueMap.put("alt", "Logo");
        }
        return true;
    }
    
    /**
     * Validates page title configuration
     */
    public boolean validatePageTitle(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("text")) {
            log.warn("Page title missing required 'text' field");
            return false;
        }
        return true;
    }
    
    /**
     * Validates banner image configuration
     */
    public boolean validateBannerImage(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("url")) {
            log.warn("Banner image missing required 'url' field");
            return false;
        }
        if (!valueMap.containsKey("alt")) {
            log.warn("Banner image missing 'alt' field, adding default");
            valueMap.put("alt", "Banner");
        }
        return true;
    }
    
    /**
     * Validates footer configuration
     */
    public boolean validateFooter(Map<String, Object> valueMap) {
        boolean valid = true;
        
        // Check for required fields
        if (!valueMap.containsKey("image")) {
            log.warn("Footer missing required 'image' field");
            valid = false;
        } else {
            // Validate image is a map with url and alt
            Object imageObj = valueMap.get("image");
            if (!(imageObj instanceof Map)) {
                log.warn("Footer 'image' field is not an object");
                valid = false;
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> imageMap = (Map<String, Object>) imageObj;
                if (!imageMap.containsKey("url")) {
                    log.warn("Footer image missing required 'url' field");
                    valid = false;
                }
                if (!imageMap.containsKey("alt")) {
                    log.warn("Footer image missing 'alt' field, adding default");
                    imageMap.put("alt", "Footer Logo");
                }
            }
        }
        
        if (!valueMap.containsKey("desc")) {
            log.warn("Footer missing required 'desc' field");
            valid = false;
        }
        
        if (!valueMap.containsKey("copyright")) {
            log.warn("Footer missing required 'copyright' field");
            valid = false;
        }
        
        return valid;
    }
    
    /**
     * Validates languages configuration
     */
    public boolean validateLanguages(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("options") || !valueMap.containsKey("default")) {
            log.warn("Languages missing required 'options' or 'default' field");
            return false;
        }
        
        // Validate options is a list
        Object optionsObj = valueMap.get("options");
        if (!(optionsObj instanceof List)) {
            log.warn("Languages 'options' field is not an array");
            return false;
        }
        
        @SuppressWarnings("unchecked")
        List<Object> options = (List<Object>) optionsObj;
        
        // Validate language options
        for (Object option : options) {
            if (!(option instanceof Map)) {
                log.warn("Language option is not an object");
                continue;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> langMap = (Map<String, Object>) option;
            
            if (!langMap.containsKey("code") || !langMap.containsKey("name")) {
                log.warn("Language option missing required 'code' or 'name' field");
                continue;
            }
            
            if (!langMap.containsKey("active")) {
                log.warn("Language option missing 'active' field, setting to true by default");
                langMap.put("active", true);
            }
        }
        
        // Validate default is a string
        if (!(valueMap.get("default") instanceof String)) {
            log.warn("Languages 'default' field is not a string");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates currencies configuration
     */
    public boolean validateCurrencies(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("options") || !valueMap.containsKey("default")) {
            log.warn("Currencies missing required 'options' or 'default' field");
            return false;
        }
        
        // Validate options is a list
        Object optionsObj = valueMap.get("options");
        if (!(optionsObj instanceof List)) {
            log.warn("Currencies 'options' field is not an array");
            return false;
        }
        
        @SuppressWarnings("unchecked")
        List<Object> options = (List<Object>) optionsObj;
        
        // Validate currency options
        for (Object option : options) {
            if (!(option instanceof Map)) {
                log.warn("Currency option is not an object");
                continue;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> currencyMap = (Map<String, Object>) option;
            
            if (!currencyMap.containsKey("code") || !currencyMap.containsKey("symbol") || !currencyMap.containsKey("name")) {
                log.warn("Currency option missing required 'code', 'symbol', or 'name' field");
                continue;
            }
            
            if (!currencyMap.containsKey("active")) {
                log.warn("Currency option missing 'active' field, setting to true by default");
                currencyMap.put("active", true);
            }
        }
        
        // Validate default is a string
        if (!(valueMap.get("default") instanceof String)) {
            log.warn("Currencies 'default' field is not a string");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates length of stay configuration
     */
    public boolean validateLengthOfStay(Map<String, Object> valueMap) {
        boolean valid = true;
        
        // Validate required fields
        if (!valueMap.containsKey("min")) {
            log.warn("Length of stay missing required 'min' field");
            valid = false;
        }
        
        if (!valueMap.containsKey("max")) {
            log.warn("Length of stay missing required 'max' field");
            valid = false;
        }
        
        if (!valueMap.containsKey("default")) {
            log.warn("Length of stay missing required 'default' field");
            valid = false;
        }
        
        // If all required fields are present, validate logical constraints
        if (valid) {
            try {
                int min = Integer.parseInt(valueMap.get("min").toString());
                int max = Integer.parseInt(valueMap.get("max").toString());
                int defaultVal = Integer.parseInt(valueMap.get("default").toString());
                
                if (min < 1) {
                    log.warn("Length of stay 'min' should be at least 1, correcting");
                    valueMap.put("min", 1);
                }
                
                if (max < min) {
                    log.warn("Length of stay 'max' should be greater than 'min', correcting");
                    valueMap.put("max", min);
                }
                
                if (defaultVal < min || defaultVal > max) {
                    log.warn("Length of stay 'default' should be between 'min' and 'max', correcting");
                    valueMap.put("default", min);
                }
            } catch (NumberFormatException e) {
                log.warn("Length of stay fields should be numbers");
                valid = false;
            }
        }
        
        return valid;
    }
    
    /**
     * Validates guest options configuration
     */
    public boolean validateGuestOptions(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("show")) {
            log.warn("Guest options missing required 'show' field");
            valueMap.put("show", true);
        }
        
        if (!valueMap.containsKey("use_guest_type_definitions")) {
            log.warn("Guest options missing 'use_guest_type_definitions' field, adding default");
            valueMap.put("use_guest_type_definitions", true);
        }
        
        return true;
    }
    
    /**
     * Validates room options configuration
     */
    public boolean validateRoomOptions(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("show")) {
            log.warn("Room options missing required 'show' field");
            valueMap.put("show", true);
        }
        
        if (!valueMap.containsKey("max_rooms")) {
            log.warn("Room options missing 'max_rooms' field, adding default");
            valueMap.put("max_rooms", 3);
        } else {
            try {
                int maxRooms = Integer.parseInt(valueMap.get("max_rooms").toString());
                if (maxRooms < 1) {
                    log.warn("Room options 'max_rooms' should be at least 1, correcting");
                    valueMap.put("max_rooms", 1);
                }
            } catch (NumberFormatException e) {
                log.warn("Room options 'max_rooms' should be a number");
                valueMap.put("max_rooms", 3);
            }
        }
        
        return true;
    }
    
    /**
     * Validates accessibility options configuration
     */
    public boolean validateAccessibilityOptions(Map<String, Object> valueMap) {
        if (!valueMap.containsKey("show")) {
            log.warn("Accessibility options missing required 'show' field");
            valueMap.put("show", true);
        }
        
        if (!valueMap.containsKey("options")) {
            log.warn("Accessibility options missing 'options' field, adding default");
            valueMap.put("options", Collections.singletonList("wheelchair"));
        } else {
            Object optionsObj = valueMap.get("options");
            if (!(optionsObj instanceof List)) {
                log.warn("Accessibility options 'options' field is not an array");
                valueMap.put("options", Collections.singletonList("wheelchair"));
            }
        }
        
        return true;
    }
    
    /**
     * Validates number of rooms configuration
     */
    public boolean validateNumberOfRooms(Map<String, Object> valueMap) {
        boolean valid = true;
        
        // Validate required fields
        if (!valueMap.containsKey("min")) {
            log.warn("Number of rooms missing required 'min' field");
            valid = false;
        }
        
        if (!valueMap.containsKey("max")) {
            log.warn("Number of rooms missing required 'max' field");
            valid = false;
        }
        
        if (!valueMap.containsKey("value")) {
            log.warn("Number of rooms missing required 'value' field");
            valid = false;
        }
        
        // If all required fields are present, validate logical constraints
        if (valid) {
            try {
                int min = Integer.parseInt(valueMap.get("min").toString());
                int max = Integer.parseInt(valueMap.get("max").toString());
                int value = Integer.parseInt(valueMap.get("value").toString());
                
                if (min < 1) {
                    log.warn("Number of rooms 'min' should be at least 1, correcting");
                    valueMap.put("min", 1);
                }
                
                if (max < min) {
                    log.warn("Number of rooms 'max' should be greater than 'min', correcting");
                    valueMap.put("max", min);
                }
                
                if (value < min || value > max) {
                    log.warn("Number of rooms 'value' should be between 'min' and 'max', correcting");
                    valueMap.put("value", min);
                }
            } catch (NumberFormatException e) {
                log.warn("Number of rooms fields should be numbers");
                valid = false;
            }
        }
        
        return valid;
    }
} 