package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.PropertyResponse;

import java.util.List;

/**
 * Service for fetching property information through GraphQL API
 */
public interface GraphQLPropertyService {
    
    /**
     * Fetch properties by their IDs
     * 
     * @param propertyIds List of property IDs to fetch
     * @return List of PropertyResponse objects containing property details
     */
    List<PropertyResponse> getPropertiesByIds(List<Integer> propertyIds);
} 