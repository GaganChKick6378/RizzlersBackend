package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.service.UrlShortenerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TinyUrlShortenerServiceTest {

    @Mock
    private WebClient webClient;
    
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;
    
    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private TinyUrlShortenerService urlShortenerService;

    @Test
    @DisplayName("TinyUrlShortenerService implements UrlShortenerService interface")
    void testInterface() {
        // This test verifies that TinyUrlShortenerService implements UrlShortenerService
        UrlShortenerService service = urlShortenerService;
        assertNotNull(service);
    }

    @Test
    @DisplayName("shortenUrl should return the shortened URL when the API call is successful")
    void shortenUrl_whenApiCallIsSuccessful_shouldReturnShortenedUrl() {
        // Simplified test to verify interface implementation
        String longUrl = "https://example.com/long/url";
        String shortUrl = "https://tinyurl.com/abc123";
        
        // Mock the WebClient chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(shortUrl));
        
        // Call method under test
        String result = urlShortenerService.shortenUrl(longUrl);
        
        // Verify result
        assertEquals(shortUrl, result);
    }
    
    @Test
    @DisplayName("shortenUrl should return the original URL when exception occurs")
    void shortenUrl_whenExceptionOccurs_shouldReturnOriginalUrl() {
        String longUrl = "https://example.com/long/url";
        
        // Mock the WebClient chain to throw an exception
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API Error")));
        
        // Call method under test
        String result = urlShortenerService.shortenUrl(longUrl);
        
        // Verify result is the original URL
        assertEquals(longUrl, result);
    }
    
    @Test
    @DisplayName("shortenUrl should return null when input URL is null")
    void shortenUrl_whenUrlIsNull_shouldReturnNull() {
        // Call method under test with null input
        String result = urlShortenerService.shortenUrl(null);
        
        // Verify result is null
        assertNull(result);
        
        // Verify WebClient wasn't called
        verifyNoInteractions(webClientBuilder);
    }
    
    @Test
    @DisplayName("shortenUrl should return empty string when input URL is empty")
    void shortenUrl_whenUrlIsEmpty_shouldReturnEmptyString() {
        // Call method under test with empty input
        String result = urlShortenerService.shortenUrl("");
        
        // Verify result is empty string
        assertEquals("", result);
        
        // Verify WebClient wasn't called
        verifyNoInteractions(webClientBuilder);
    }

    @Test
    @DisplayName("getLongUrl should return null as it's not supported in the implementation")
    void getLongUrl_shouldReturnNull() {
        // Call method under test
        String result = urlShortenerService.getLongUrl("https://tinyurl.com/abc123");
        
        // Verify result is null, as the implementation doesn't support this feature
        assertNull(result);
    }
} 