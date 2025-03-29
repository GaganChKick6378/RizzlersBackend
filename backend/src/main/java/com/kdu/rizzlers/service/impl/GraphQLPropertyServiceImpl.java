package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.out.PropertyResponse;
import com.kdu.rizzlers.service.GraphQLPropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphQLPropertyServiceImpl implements GraphQLPropertyService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${graphql.endpoint}")
    private String graphqlUrl;
    
    @Value("${graphql.api-key}")
    private String apiKey;
    
    @Value("${graphql.api-key-header}")
    private String apiKeyHeader;
    
    @Value("${graphql.timeout:30000}")
    private int timeout;

    @Override
    public List<PropertyResponse> getPropertiesByIds(List<Integer> propertyIds) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            log.debug("No property IDs provided, returning empty list");
            return new ArrayList<>();
        }
        
        try {
            log.debug("Making POST request to GraphQL endpoint: {}", graphqlUrl);
            
            // Create a simple GraphQL query to list all properties, not filtering by IDs to ensure we get all properties
            String query = "{ listProperties { property_id property_name property_address contact_number } }";
            
            // Build the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            
            // Log the request for debugging
            log.debug("GraphQL Query: {}", query);
            log.debug("Using API Key Header: {}, API Key: {}", apiKeyHeader, apiKey);
            
            // Build WebClient and make POST request
            WebClient client = webClientBuilder.build();
            
            String response = client
                .post()
                .uri(graphqlUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header(apiKeyHeader, apiKey) // Use the header name from properties
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMillis(timeout));
            
            if (response != null) {
                log.debug("Received response from GraphQL endpoint");
                
                // Parse the JSON response
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.path("data");
                JsonNode properties = data.path("listProperties");
                
                if (properties.isArray()) {
                    log.info("Found {} properties in response", properties.size());
                    
                    List<PropertyResponse> result = new ArrayList<>();
                    for (JsonNode property : properties) {
                        Integer id = property.path("property_id").asInt();
                        
                        // Include all properties that are in our input list
                        if (propertyIds.contains(id)) {
                            PropertyResponse propertyResponse = PropertyResponse.builder()
                                .propertyId(id)
                                .propertyName(property.path("property_name").asText())
                                .propertyAddress(property.path("property_address").asText())
                                .contactNumber(property.path("contact_number").asText())
                                .tenantId(1) // Default value
                                .build();
                            
                            result.add(propertyResponse);
                            log.debug("Added property: ID={}, Name={}", 
                                id, property.path("property_name").asText());
                        }
                    }
                    
                    return result;
                } else {
                    log.warn("listProperties is not an array in the GraphQL response");
                }
            } else {
                log.warn("Received null response from GraphQL endpoint");
            }
        } catch (Exception e) {
            log.error("Error fetching properties from GraphQL: {}", e.getMessage(), e);
        }
        
        return new ArrayList<>();
    }
} 