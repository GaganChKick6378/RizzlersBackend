package com.kdu.rizzlers.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    
    @Value("${graphql.endpoint}")
    private String graphqlUrl;
    
    @Value("${graphql.api-key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @GetMapping("/graphql")
    public ResponseEntity<String> testGraphQLConnection() {
        log.info("Testing GraphQL connection to: {}", graphqlUrl);
        
        StringBuilder result = new StringBuilder();
        
        try {
            // Try multiple approaches to connect to the GraphQL server
            
            // Try POST request with application/json content type and proper GraphQL query
            try {
                log.info("Testing POST request to GraphQL endpoint");
                
                Map<String, Object> requestBody = new HashMap<>();
                String query = "{ listProperties { property_id property_name property_address } }";
                requestBody.put("query", query);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-api-key", apiKey);
                
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                
                ResponseEntity<String> postResponse = restTemplate.exchange(
                        graphqlUrl, 
                        HttpMethod.POST,
                        entity,
                        String.class);
                
                result.append("POST with API Key Status: ").append(postResponse.getStatusCode())
                      .append("\nPOST with API Key Body: ").append(postResponse.getBody()).append("\n\n");
                
                log.info("POST response status: {}", postResponse.getStatusCode());
                log.info("POST response body: {}", postResponse.getBody());
            } catch (Exception e) {
                result.append("POST with API Key failed: ").append(e.getMessage()).append("\n\n");
                log.error("POST request failed: ", e);
                
                // Try without API key
                try {
                    log.info("Testing POST request without API key");
                    
                    Map<String, Object> requestBody = new HashMap<>();
                    String query = "{ listProperties { property_id property_name property_address } }";
                    requestBody.put("query", query);
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    
                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                    
                    ResponseEntity<String> postResponse = restTemplate.exchange(
                            graphqlUrl, 
                            HttpMethod.POST,
                            entity,
                            String.class);
                    
                    result.append("POST without API Key Status: ").append(postResponse.getStatusCode())
                          .append("\nPOST without API Key Body: ").append(postResponse.getBody()).append("\n\n");
                    
                    log.info("POST without API Key response status: {}", postResponse.getStatusCode());
                    log.info("POST without API Key response body: {}", postResponse.getBody());
                } catch (Exception ex) {
                    result.append("POST without API Key failed: ").append(ex.getMessage()).append("\n\n");
                    log.error("POST without API Key request failed: ", ex);
                }
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(result.toString());
        } catch (Exception e) {
            log.error("Error testing GraphQL connection: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("GraphQL Connection Test Failed: " + e.getMessage());
        }
    }
} 