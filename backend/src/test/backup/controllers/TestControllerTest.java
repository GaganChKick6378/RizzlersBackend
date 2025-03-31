package com.kdu.rizzlers.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(TestController.class)
@AutoConfigureMockMvc(addFilters = false)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private TestController testController;
    
    @MockBean
    private RestTemplate restTemplate;
    
    private String graphqlUrl = "https://example.com/graphql";
    private String apiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        // Use reflection to set the private fields
        ReflectionTestUtils.setField(testController, "graphqlUrl", graphqlUrl);
        ReflectionTestUtils.setField(testController, "apiKey", apiKey);
        ReflectionTestUtils.setField(testController, "restTemplate", restTemplate);
    }

    @Test
    @DisplayName("Test GraphQL connection with successful response")
    void testGraphQLConnection_withSuccessfulResponse() throws Exception {
        // Arrange
        ResponseEntity<String> getResponse = new ResponseEntity<>("GET Success", HttpStatus.OK);
        ResponseEntity<String> postResponse = new ResponseEntity<>("POST Success", HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(getResponse);
                
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(postResponse);

        // Act & Assert
        mockMvc.perform(get("/test/graphql-connection")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GET Status: 200 OK")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GET Body: GET Success")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("POST Status: 200 OK")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("POST Body: POST Success")));
                
        // Verify that RestTemplate was called with the right parameters
        verify(restTemplate, times(1)).exchange(
                eq(graphqlUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class));
                
        verify(restTemplate, times(1)).exchange(
                eq(graphqlUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    @DisplayName("Test GraphQL connection with GET failure and POST success")
    void testGraphQLConnection_withGetFailureAndPostSuccess() throws Exception {
        // Arrange
        ResponseEntity<String> postResponse = new ResponseEntity<>("POST Success", HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RuntimeException("GET Failed"));
                
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(postResponse);

        // Act & Assert
        mockMvc.perform(get("/test/graphql-connection")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GET Failed")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("POST Status: 200 OK")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("POST Body: POST Success")));
    }

    @Test
    @DisplayName("Test GraphQL connection with POST failure")
    void testGraphQLConnection_withPostFailure() throws Exception {
        // Arrange
        ResponseEntity<String> getResponse = new ResponseEntity<>("GET Success", HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(getResponse);
                
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RuntimeException("POST Failed"));

        // Act & Assert
        mockMvc.perform(get("/test/graphql-connection")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GET Status: 200 OK")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GET Body: GET Success")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("POST with API Key failed")));
                
        // Verify that a second POST attempt was made without the API key
        verify(restTemplate, times(2)).exchange(
                eq(graphqlUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    @DisplayName("Test GraphQL connection with complete failure")
    void testGraphQLConnection_withCompleteFailure() throws Exception {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RuntimeException("Connection Failed"));

        // Act & Assert
        mockMvc.perform(get("/test/graphql-connection")
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GraphQL Connection Test Failed")));
    }
} 