package com.kdu.rizzlers.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AwsHealthCheckController.class)
@AutoConfigureMockMvc(addFilters = false)
class AwsHealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Health check endpoint should return UP status")
    void healthCheck_shouldReturnUpStatus() throws Exception {
        mockMvc.perform(get("/health")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Health check endpoint should be fast and lightweight")
    void healthCheck_shouldBeFastAndLightweight() throws Exception {
        // Act & Assert - Verify response time is under a threshold
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/health")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
                
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // The response should be very fast (less than 50ms)
        // This is an arbitrary threshold, adjust as needed
        assert executionTime < 50 : "Health check took too long: " + executionTime + "ms";
    }
} 