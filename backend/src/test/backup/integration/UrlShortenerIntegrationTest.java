package com.kdu.rizzlers.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.controller.UrlShortenerController;
import com.kdu.rizzlers.dto.in.UrlShortenRequest;
import com.kdu.rizzlers.dto.out.UrlShortenResponse;
import com.kdu.rizzlers.service.UrlShortenerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UrlShortenerController.class)
@WithMockUser
public class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlShortenerService urlShortenerService;

    @Test
    @DisplayName("Should process URL shortening through the entire flow")
    public void shortenUrl_shouldProcessThroughEntireFlow() throws Exception {
        // Arrange
        String originalUrl = "https://www.example.com/very/long/path/to/shorten";
        String shortenedUrl = "https://tinyurl.com/abcd123";
        
        UrlShortenRequest request = new UrlShortenRequest();
        request.setUrl(originalUrl);
        
        UrlShortenResponse expectedResponse = new UrlShortenResponse();
        expectedResponse.setOriginalUrl(originalUrl);
        expectedResponse.setShortUrl(shortenedUrl);
        
        when(urlShortenerService.shortenUrl(any(String.class))).thenReturn(shortenedUrl);

        // Act & Assert
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortUrl").value(shortenedUrl));
    }

    @Test
    @DisplayName("Should return bad request for invalid URL")
    public void shortenUrl_withInvalidUrl_shouldReturnBadRequest() throws Exception {
        // Arrange
        UrlShortenRequest request = new UrlShortenRequest();
        request.setUrl("invalid-url-format");

        // Act & Assert
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
} 