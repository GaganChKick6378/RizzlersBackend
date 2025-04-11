package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.service.PromotionGraphQLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PromotionGraphQLServiceImpl implements PromotionGraphQLService {

    private final WebClient webClient;
    private final HttpGraphQlClient graphQlClient;

    private static final String GET_ALL_PROMOTIONS_QUERY = "query { listPromotions { " +
            "promotion_id " +
            "promotion_title " +
            "promotion_description " +
            "price_factor " +
            "minimum_days_of_stay " +
            "is_deactivated " +
            "} }";
            
    private static final String GET_PROMOTION_BY_ID_QUERY = 
            "query MyQuery($promotionId: Int!) {" +
            "  getPromotion(where: {promotion_id: $promotionId}) {" +
            "    price_factor" +
            "    promotion_title" +
            "    promotion_description" +
            "  }" +
            "}";

    public PromotionGraphQLServiceImpl(@Value("${graphql.endpoint}") String graphqlEndpoint,
                                 @Value("${graphql.api-key:}") String apiKey,
                                 @Value("${graphql.api-key-header:X-Api-Key}") String apiKeyHeader) {
        this.webClient = WebClient.builder()
                .baseUrl(graphqlEndpoint)
                .defaultHeader(apiKeyHeader, apiKey)
                .build();
        
        this.graphQlClient = HttpGraphQlClient.builder(webClient)
                .build();
    }

    @Override
    public List<PromotionDTO> fetchAllPromotions() {
        try {
            // Define GraphQL document 
            String document = GET_ALL_PROMOTIONS_QUERY;
            
            // Execute the query using HttpGraphQlClient
            return graphQlClient.document(document)
                    .retrieve("listPromotions")
                    .toEntityList(PromotionDTO.class)
                    .onErrorResume(e -> {
                        log.error("Error fetching promotions from GraphQL", e);
                        return Mono.just(new ArrayList<>());
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error fetching promotions from GraphQL", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> fetchPromotion(Integer promotionId) {
        log.info("Fetching promotion with ID: {} from GraphQL", promotionId);
        try {
            // Create variables map for the query
            Map<String, Object> variables = new HashMap<>();
            variables.put("promotionId", promotionId);
            
            // Execute the query using HttpGraphQlClient
            Map<String, Object> promotionData = graphQlClient.document(GET_PROMOTION_BY_ID_QUERY)
                    .variables(variables)
                    .retrieve("getPromotion")
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .onErrorResume(e -> {
                        log.error("Error fetching promotion from GraphQL: {}", e.getMessage(), e);
                        return Mono.just(new HashMap<>());
                    })
                    .block();
            
            if (promotionData == null || promotionData.isEmpty()) {
                log.warn("No promotion found with ID: {}", promotionId);
                return new HashMap<>();
            }
            
            log.info("Successfully fetched promotion: {}", promotionData);
            return promotionData;
            
        } catch (Exception e) {
            log.error("Error fetching promotion from GraphQL: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
} 