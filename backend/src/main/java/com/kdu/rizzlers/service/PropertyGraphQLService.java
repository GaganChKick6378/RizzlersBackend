package com.kdu.rizzlers.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.entity.Property;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class PropertyGraphQLService {

    private WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${graphql.endpoint}")
    private String graphqlEndpoint;
    
    @Value("${graphql.api-key}")
    private String apiKey;

    public PropertyGraphQLService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void initWebClient() {
        log.info("Initializing WebClient with endpoint: {} (apiKey length: {})", 
                graphqlEndpoint, 
                apiKey != null ? apiKey.length() : 0);
        this.webClient = WebClient.builder()
                .baseUrl(graphqlEndpoint)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }

    public Mono<Property> getPropertyByName(String propertyName) {
        String query = "query FindPropertyByName { getProperty(where: { property_name: \"" + propertyName + "\" }) { property_id property_name property_address contact_number } }";
        
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("query", query))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(responseStr -> log.debug("Raw GraphQL response for property by name: {}", responseStr))
                .map(responseStr -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(responseStr);
                        JsonNode dataNode = rootNode.get("data");

                        if (dataNode == null || dataNode.isNull()) {
                            log.error("No data found in response: {}", responseStr);
                            return null;
                        }

                        JsonNode propertyNode = dataNode.get("getProperty");
                        if (propertyNode == null || propertyNode.isNull()) {
                            log.error("No property found with name {}: {}", propertyName, dataNode);
                            return null;
                        }

                        Property property = new Property();
                        property.setProperty_id(propertyNode.get("property_id").asInt());
                        property.setProperty_name(propertyNode.get("property_name").asText());
                        property.setProperty_address(propertyNode.get("property_address").asText());
                        property.setContact_number(propertyNode.get("contact_number").asText());
                        
                        // tenant_id might not be included in this query
                        if (propertyNode.has("tenant_id")) {
                            property.setTenant_id(propertyNode.get("tenant_id").asInt());
                        }

                        return property;
                    } catch (Exception e) {
                        log.error("Error parsing GraphQL response for property by name: {}", e.getMessage(), e);
                        return null;
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error calling GraphQL API for property by name: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }
}