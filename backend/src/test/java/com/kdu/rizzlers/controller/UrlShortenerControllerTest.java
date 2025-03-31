package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.in.UrlShortenRequest;
import com.kdu.rizzlers.dto.out.UrlShortenResponse;
import com.kdu.rizzlers.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
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

    @BeforeEach
    void setup() {
        validRequest = UrlShortenRequest.builder()
                .url("https://www.example.com/very/long/path/to/shorten")
                .build();

        invalidRequest = UrlShortenRequest.builder()
                .url("invalid-url-format")
                .build();
    }

    @Test
    @DisplayName("Should successfully shorten a valid URL")
    void shortenUrl_withValidUrl_returnsShortUrl() throws Exception {
        // Arrange
        String shortenedUrl = "https://tinyurl.com/abcd123";
        Mockito.when(urlShortenerService.shortenUrl(anyString())).thenReturn(shortenedUrl);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/url/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.originalUrl", is(validRequest.getUrl())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.shortUrl", is(shortenedUrl)));
    }

    @Test
    @DisplayName("Should return error for invalid URL format")
    void shortenUrl_withInvalidUrl_returnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/url/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle exception when service throws an error")
    void shortenUrl_whenServiceFails_returnsErrorResponse() throws Exception {
        // Arrange
        Mockito.when(urlShortenerService.shortenUrl(anyString())).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/url/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error", is("Failed to process request")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", is("Service failure")));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shortenUrl_withEmptyBody_returnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/url/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
} 