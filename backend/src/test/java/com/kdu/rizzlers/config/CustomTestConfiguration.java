package com.kdu.rizzlers.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@PropertySource("classpath:application-test.yml")
@EntityScan(basePackages = "com.kdu.rizzlers.entity")
@EnableJpaRepositories(basePackages = "com.kdu.rizzlers.repository")
@EnableJpaAuditing
public class CustomTestConfiguration {

    /**
     * Provides a mocked WebClient.Builder for tests
     */
    @Bean
    @Primary
    public WebClient.Builder webClientBuilder() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        WebClient webClient = mock(WebClient.class);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        
        when(builder.baseUrl(any(String.class))).thenReturn(builder);
        when(builder.defaultHeader(any(String.class), any(String.class))).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(Collections.emptyMap()));
        
        return builder;
    }
    
    /**
     * Provides a mocked HttpGraphQlClient for tests
     */
    @Bean
    @Primary
    public HttpGraphQlClient httpGraphQlClient() {
        // Create a more robust mock that returns proper responses for GraphQL
        HttpGraphQlClient mockClient = mock(HttpGraphQlClient.class);
        HttpGraphQlClient.Builder builder = mock(HttpGraphQlClient.Builder.class);
        HttpGraphQlClient.RequestSpec requestSpec = mock(HttpGraphQlClient.RequestSpec.class);
        
        // Create a mock ClientGraphQlResponse
        ClientGraphQlResponse graphQlResponse = mock(ClientGraphQlResponse.class);
        
        // Create empty response data
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("dailyRates", Collections.emptyList());
        
        // Setup the complete chain of mocks
        when(mockClient.document(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.variable(any(String.class), any())).thenReturn(requestSpec);
        
        // Mock the execute() method to return a Mono<ClientGraphQlResponse>
        when(requestSpec.execute()).thenReturn(Mono.just(graphQlResponse));
        
        // Mock the ClientGraphQlResponse methods
        when(graphQlResponse.getData()).thenReturn(emptyData);
        when(graphQlResponse.getErrors()).thenReturn(Collections.<ResponseError>emptyList());
        when(graphQlResponse.isValid()).thenReturn(true);
        
        when(mockClient.mutate()).thenReturn(builder);
        when(builder.build()).thenReturn(mockClient);
        
        return mockClient;
    }
} 