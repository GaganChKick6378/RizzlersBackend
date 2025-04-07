package com.kdu.rizzlers.dto.graphql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for GraphQL API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLResponseDTO<T> {
    private T data;
} 