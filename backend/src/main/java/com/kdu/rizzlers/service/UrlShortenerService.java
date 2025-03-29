package com.kdu.rizzlers.service;

/**
 * Service interface for URL shortening operations.
 */
public interface UrlShortenerService {
    
    /**
     * Shortens a URL using an external URL shortening service.
     *
     * @param longUrl The original long URL to be shortened
     * @return The shortened URL
     */
    String shortenUrl(String longUrl);
    
    /**
     * Gets the original URL from a shortened URL.
     * Note: This may not be supported by all URL shortening services.
     *
     * @param shortUrl The shortened URL
     * @return The original long URL if available, otherwise null
     */
    String getLongUrl(String shortUrl);
}