package com.kdu.rizzlers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the UrlShortenerService interface to ensure proper contract behavior
 */
@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTest {

    @Mock
    private UrlShortenerService urlShortenerService;
    
    private final String longUrl = "https://example.com/very/long/url/that/needs/shortening";
    private final String shortUrl = "abc123";

    @BeforeEach
    void setUp() {
        // Using lenient() to avoid unnecessary stubbing errors
        lenient().when(urlShortenerService.shortenUrl(longUrl)).thenReturn(shortUrl);
        lenient().when(urlShortenerService.getLongUrl(shortUrl)).thenReturn(longUrl);
        
        // Null/empty cases
        lenient().when(urlShortenerService.shortenUrl(null)).thenReturn(null);
        lenient().when(urlShortenerService.shortenUrl("")).thenReturn("");
        lenient().when(urlShortenerService.getLongUrl(null)).thenReturn(null);
        lenient().when(urlShortenerService.getLongUrl("invalid")).thenReturn(null);
    }

    @Test
    @DisplayName("Should convert a long URL to a shortened version")
    void shortenUrl_shouldConvertLongToShortUrl() {
        // Act
        String result = urlShortenerService.shortenUrl(longUrl);
        
        // Assert
        assertEquals(shortUrl, result);
        assertNotEquals(longUrl, result);
        assertTrue(result.length() < longUrl.length());
    }
    
    @Test
    @DisplayName("Should retrieve the original URL from a short URL")
    void getLongUrl_shouldRetrieveOriginalUrl() {
        // Act
        String result = urlShortenerService.getLongUrl(shortUrl);
        
        // Assert
        assertEquals(longUrl, result);
    }
    
    @Test
    @DisplayName("Should handle null input gracefully when shortening URLs")
    void shortenUrl_withNullInput_shouldHandleGracefully() {
        // Act
        String result = urlShortenerService.shortenUrl(null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should handle empty input gracefully when shortening URLs")
    void shortenUrl_withEmptyInput_shouldReturnEmpty() {
        // Act
        String result = urlShortenerService.shortenUrl("");
        
        // Assert
        assertEquals("", result);
    }
    
    @Test
    @DisplayName("Should handle null input gracefully when retrieving long URLs")
    void getLongUrl_withNullInput_shouldReturnNull() {
        // Act
        String result = urlShortenerService.getLongUrl(null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should return null for invalid short URLs")
    void getLongUrl_withInvalidShortUrl_shouldReturnNull() {
        // Act
        String result = urlShortenerService.getLongUrl("invalid");
        
        // Assert
        assertNull(result);
    }
} 