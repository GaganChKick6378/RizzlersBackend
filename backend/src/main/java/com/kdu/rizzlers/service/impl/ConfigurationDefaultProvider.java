package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.ResultsPageConfigResponse;
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

    /**
     * Sets default values for results page configuration fields to ensure the response never has null values
     *
     * @param builder The response builder to set defaults on
     */
    public void setResultsPageDefaultValues(ResultsPageConfigResponse.ResultsPageConfigResponseBuilder builder) {
        // Default filters
        Map<String, Object> defaultFilters = new HashMap<>();
        defaultFilters.put("enabled", true);
        defaultFilters.put("position", "left");
        
        // Default filter sections
        List<Map<String, Object>> defaultSections = new ArrayList<>();
        
        // Price range filter
        Map<String, Object> priceFilter = new HashMap<>();
        priceFilter.put("id", "price");
        priceFilter.put("type", "range");
        priceFilter.put("label", "Price Range");
        priceFilter.put("enabled", true);
        Map<String, Object> priceOptions = new HashMap<>();
        priceOptions.put("min", 0);
        priceOptions.put("max", 1000);
        priceOptions.put("step", 10);
        priceFilter.put("options", priceOptions);
        defaultSections.add(priceFilter);
        
        // Amenities filter
        Map<String, Object> amenitiesFilter = new HashMap<>();
        amenitiesFilter.put("id", "amenities");
        amenitiesFilter.put("type", "checkbox");
        amenitiesFilter.put("label", "Amenities");
        amenitiesFilter.put("enabled", true);
        List<Map<String, Object>> amenityOptions = new ArrayList<>();
        Map<String, Object> wifiOption = new HashMap<>();
        wifiOption.put("id", "wifi");
        wifiOption.put("label", "WiFi");
        amenityOptions.add(wifiOption);
        Map<String, Object> poolOption = new HashMap<>();
        poolOption.put("id", "pool");
        poolOption.put("label", "Swimming Pool");
        amenityOptions.add(poolOption);
        amenitiesFilter.put("options", amenityOptions);
        defaultSections.add(amenitiesFilter);
        
        defaultFilters.put("sections", defaultSections);
        builder.filters(defaultFilters);
        
        // Default sorting
        Map<String, Object> defaultSorting = new HashMap<>();
        defaultSorting.put("enabled", true);
        defaultSorting.put("default", "price_low_high");
        List<Map<String, Object>> sortOptions = new ArrayList<>();
        
        Map<String, Object> priceLowHighSort = new HashMap<>();
        priceLowHighSort.put("id", "price_low_high");
        priceLowHighSort.put("label", "Price: Low to High");
        sortOptions.add(priceLowHighSort);
        
        Map<String, Object> priceHighLowSort = new HashMap<>();
        priceHighLowSort.put("id", "price_high_low");
        priceHighLowSort.put("label", "Price: High to Low");
        sortOptions.add(priceHighLowSort);
        
        Map<String, Object> ratingSort = new HashMap<>();
        ratingSort.put("id", "rating");
        ratingSort.put("label", "Rating");
        sortOptions.add(ratingSort);
        
        defaultSorting.put("options", sortOptions);
        builder.sorting(defaultSorting);
        
        // Default pagination
        Map<String, Object> defaultPagination = new HashMap<>();
        defaultPagination.put("enabled", true);
        defaultPagination.put("default_size", 10);
        List<Integer> pageSizeOptions = Arrays.asList(5, 10, 20, 50);
        defaultPagination.put("size_options", pageSizeOptions);
        builder.pagination(defaultPagination);
        
        // Default display options
        Map<String, Object> defaultDisplayOptions = new HashMap<>();
        defaultDisplayOptions.put("layout", "grid");
        defaultDisplayOptions.put("show_image_gallery", true);
        defaultDisplayOptions.put("show_ratings", true);
        defaultDisplayOptions.put("show_amenities", true);
        defaultDisplayOptions.put("max_amenities_shown", 3);
        defaultDisplayOptions.put("show_description", true);
        builder.displayOptions(defaultDisplayOptions);
    }
} 