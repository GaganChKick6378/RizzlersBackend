package com.kdu.rizzlers.service;

import java.util.Map;

/**
 * Service for executing generic GraphQL queries and mutations
 */
public interface GraphQLService {
    
    /**
     * Execute a GraphQL query or mutation
     * 
     * @param query The GraphQL query or mutation to execute
     * @return Map containing the response data
     */
    Map<String, Object> executeQuery(String query);
    
    /**
     * Execute a GraphQL query or mutation with variables
     * 
     * @param query The GraphQL query or mutation to execute
     * @param variables The variables to include in the query
     * @return Map containing the response data
     */
    Map<String, Object> executeQuery(String query, Map<String, Object> variables);
} 