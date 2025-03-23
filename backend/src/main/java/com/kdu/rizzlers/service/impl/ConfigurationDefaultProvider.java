package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provider for default configuration values.
 * Centralizes all default configuration values in one place.
 */
@Component
public class ConfigurationDefaultProvider {

    /**
     * Sets default values for all configuration fields to ensure the response never has null values
     *
     * @param builder The response builder to set defaults on
     */
    public void setDefaultConfigValues(LandingPageConfigResponse.LandingPageConfigResponseBuilder builder) {
        // Default header logo
        Map<String, Object> defaultHeaderLogo = new HashMap<>();
        defaultHeaderLogo.put("url", "https://example.com/default-logo.png");
        defaultHeaderLogo.put("alt", "Default Logo");
        builder.headerLogo(defaultHeaderLogo);
        
        // Default page title
        Map<String, Object> defaultPageTitle = new HashMap<>();
        defaultPageTitle.put("text", "Internet Booking Engine");
        builder.pageTitle(defaultPageTitle);
        
        // Default banner image
        Map<String, Object> defaultBannerImage = new HashMap<>();
        defaultBannerImage.put("url", "https://example.com/default-banner.jpg");
        defaultBannerImage.put("alt", "Default Banner");
        builder.bannerImage(defaultBannerImage);
        
        // Default footer
        Map<String, Object> defaultFooter = new HashMap<>();
        Map<String, Object> defaultFooterImage = new HashMap<>();
        defaultFooterImage.put("url", "https://example.com/default-footer-logo.png");
        defaultFooterImage.put("alt", "Default Footer Logo");
        defaultFooter.put("image", defaultFooterImage);
        defaultFooter.put("desc", "Your trusted travel partner");
        defaultFooter.put("copyright", "© " + Calendar.getInstance().get(Calendar.YEAR) + " Company Name");
        builder.footer(defaultFooter);
        
        // Default languages
        Map<String, Object> defaultLanguages = new HashMap<>();
        List<Map<String, Object>> defaultLanguageOptions = new ArrayList<>();
        Map<String, Object> defaultEnglish = new HashMap<>();
        defaultEnglish.put("code", "EN");
        defaultEnglish.put("name", "English");
        defaultEnglish.put("active", true);
        defaultLanguageOptions.add(defaultEnglish);
        defaultLanguages.put("options", defaultLanguageOptions);
        defaultLanguages.put("default", "EN");
        builder.languages(defaultLanguages);
        
        // Default currencies
        Map<String, Object> defaultCurrencies = new HashMap<>();
        List<Map<String, Object>> defaultCurrencyOptions = new ArrayList<>();
        Map<String, Object> defaultUSD = new HashMap<>();
        defaultUSD.put("code", "USD");
        defaultUSD.put("symbol", "$");
        defaultUSD.put("name", "US Dollar");
        defaultUSD.put("active", true);
        defaultCurrencyOptions.add(defaultUSD);
        defaultCurrencies.put("options", defaultCurrencyOptions);
        defaultCurrencies.put("default", "USD");
        builder.currencies(defaultCurrencies);
        
        // Default length of stay
        Map<String, Object> defaultLengthOfStay = new HashMap<>();
        defaultLengthOfStay.put("min", 1);
        defaultLengthOfStay.put("max", 7);
        defaultLengthOfStay.put("default", 1);
        builder.lengthOfStay(defaultLengthOfStay);
        
        // Default guest options
        Map<String, Object> defaultGuestOptions = new HashMap<>();
        defaultGuestOptions.put("show", true);
        defaultGuestOptions.put("use_guest_type_definitions", true);
        builder.guestOptions(defaultGuestOptions);
        
        // Default room options
        Map<String, Object> defaultRoomOptions = new HashMap<>();
        defaultRoomOptions.put("show", true);
        defaultRoomOptions.put("max_rooms", 3);
        builder.roomOptions(defaultRoomOptions);
        
        // Default accessibility options
        Map<String, Object> defaultAccessibilityOptions = new HashMap<>();
        defaultAccessibilityOptions.put("show", true);
        List<String> defaultAccessibilityList = Collections.singletonList("wheelchair");
        defaultAccessibilityOptions.put("options", defaultAccessibilityList);
        builder.accessibilityOptions(defaultAccessibilityOptions);
        
        // Default number of rooms
        Map<String, Object> defaultNumberOfRooms = new HashMap<>();
        defaultNumberOfRooms.put("min", 1);
        defaultNumberOfRooms.put("max", 3);
        defaultNumberOfRooms.put("value", 1);
        builder.numberOfRooms(defaultNumberOfRooms);
    }
} 