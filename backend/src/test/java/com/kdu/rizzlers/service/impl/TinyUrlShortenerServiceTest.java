package com.kdu.rizzlers.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TinyUrlShortenerServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private TinyUrlShortenerService urlShortenerService;
    
    private final String LONG_URL = "https://www.example.com/very/long/path/to/shorten";
    private final String SHORT_URL = "https://tinyurl.com/abcd123";
    private final String TINYURL_API_ENDPOINT = "https://tinyurl.com/api-create.php?url=";

    @BeforeEach
    void setup() {
        urlShortenerService = new TinyUrlShortenerService(webClientBuilder);
        
        // Mock the WebClient chain with lenient mode to avoid "unnecessary stubbing" errors
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("Should successfully shorten a URL")
    void shortenUrl_shouldReturnShortUrl() {
        // Arrange
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(SHORT_URL));
        
        // Act
        String result = urlShortenerService.shortenUrl(LONG_URL);
        
        // Assert
        assertEquals(SHORT_URL, result);
        verify(webClientBuilder, times(1)).build();
        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri(TINYURL_API_ENDPOINT + LONG_URL);
    }

    @Test
    @DisplayName("Should return original URL when API call fails")
    void shortenUrl_whenApiFails_shouldReturnOriginalUrl() {
        // Arrange
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API Error")));
        
        // Act
        String result = urlShortenerService.shortenUrl(LONG_URL);
        
        // Assert
        assertEquals(LONG_URL, result, "Should return the original URL when API call fails");
        verify(webClientBuilder, times(1)).build();
        verify(webClient, times(1)).get();
    }

    @Test
    @DisplayName("Should return null when getting long URL as it's not supported")
    void getLongUrl_shouldReturnNull() {
        // Act
        String result = urlShortenerService.getLongUrl(SHORT_URL);
        
        // Assert
        assertNull(result, "Should return null as this operation is not supported");
        verifyNoInteractions(webClientBuilder, webClient);
    }
    
    @Test
    @DisplayName("Should return original URL when input is null")
    void shortenUrl_withNullInput_shouldReturnNull() {
        // Act
        String result = urlShortenerService.shortenUrl(null);
        
        // Assert
        assertNull(result, "Should return null when input URL is null");
        verifyNoInteractions(webClientBuilder, webClient);
    }
    
    @Test
    @DisplayName("Should return original URL when input is empty")
    void shortenUrl_withEmptyInput_shouldReturnEmpty() {
        // Act
        String result = urlShortenerService.shortenUrl("");
        
        // Assert
        assertEquals("", result, "Should return empty string when input URL is empty");
        verifyNoInteractions(webClientBuilder, webClient);
    }
} 