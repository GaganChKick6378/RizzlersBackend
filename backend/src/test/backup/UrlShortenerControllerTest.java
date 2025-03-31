package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.in.UrlShortenRequest;
import com.kdu.rizzlers.dto.out.UrlShortenResponse;
import com.kdu.rizzlers.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UrlShortenerController.class)
@AutoConfigureMockMvc(addFilters = false)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlShortenerService urlShortenerService;

    private UrlShortenRequest validRequest;
    private UrlShortenRequest invalidRequest;
    private String originalUrl;
    private String shortUrl;

    @BeforeEach
    void setUp() {
        originalUrl = "https://example.com/very/long/url/path/to/be/shortened";
        shortUrl = "https://short.domain/abc123";

        // Create valid request
        validRequest = new UrlShortenRequest();
        validRequest.setUrl(originalUrl);

        // Create invalid request
        invalidRequest = new UrlShortenRequest();
        invalidRequest.setUrl("invalid-url");
    }

    @Test
    @DisplayName("Should shorten URL with valid request")
    void shortenUrl_withValidRequest_shouldReturnShortenedUrl() throws Exception {
        // Arrange
        when(urlShortenerService.shortenUrl(anyString())).thenReturn(shortUrl);

        // Act & Assert
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl", is(originalUrl)))
                .andExpect(jsonPath("$.shortUrl", is(shortUrl)));
    }

    @Test
    @DisplayName("Should handle invalid URL format")
    void shortenUrl_withInvalidUrl_shouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid URL format"))
                .when(urlShortenerService).shortenUrl("invalid-url");

        // Act & Assert
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Failed to process request")))
                .andExpect(jsonPath("$.message", is("Invalid URL format")));
    }

    @Test
    @DisplayName("Should handle missing URL in request")
    void shortenUrl_withMissingUrl_shouldReturnBadRequest() throws Exception {
        // Arrange
        UrlShortenRequest emptyRequest = new UrlShortenRequest();
        // Don't set URL

        // Act & Assert
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exceptions")
    void shortenUrl_withServiceException_shouldReturnErrorResponse() throws Exception {
        // Arrange
        when(urlShortenerService.shortenUrl(anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Failed to process request")))
                .andExpect(jsonPath("$.message", is("Service unavailable")));
    }
} 