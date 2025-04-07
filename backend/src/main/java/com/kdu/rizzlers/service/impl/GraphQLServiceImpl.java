package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.service.GraphQLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GraphQLServiceImpl implements GraphQLService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GraphQLServiceImpl(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${graphql.endpoint}") String graphqlEndpoint,
            @Value("${graphql.api-key}") String apiKey,
            @Value("${graphql.api-key-header:X-Api-Key}") String apiKeyHeader) {
        
        this.webClient = webClientBuilder
                .baseUrl(graphqlEndpoint)
                .defaultHeader(apiKeyHeader, apiKey)
                .build();
        
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> executeQuery(String query) {
        return executeQuery(query, Collections.emptyMap());
    }

    @Override
    public Map<String, Object> executeQuery(String query, Map<String, Object> variables) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        
        if (variables != null && !variables.isEmpty()) {
            requestBody.put("variables", variables);
        }
        
        log.debug("Executing GraphQL query: {}", query);
        
        try {
            return webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .onErrorResume(e -> {
                        log.error("Error executing GraphQL query: {}", e.getMessage());
                        return Mono.just(Collections.emptyMap());
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error executing GraphQL query", e);
            return Collections.emptyMap();
        }
    }
} 