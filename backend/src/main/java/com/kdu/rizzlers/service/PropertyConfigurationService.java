package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.PropertyConfigurationDTO;
import java.util.List;

public interface PropertyConfigurationService {
    
    /**
     * Create a new property configuration
     * @param request The property configuration data
     * @return The created property configuration
     */
    PropertyConfigurationDTO.Response createPropertyConfiguration(PropertyConfigurationDTO.Request request);
    
    /**
     * Update an existing property configuration
     * @param id The ID of the property configuration to update
     * @param request The updated property configuration data
     * @return The updated property configuration
     */
    PropertyConfigurationDTO.Response updatePropertyConfiguration(Long id, PropertyConfigurationDTO.Request request);
    
    /**
     * Get a property configuration by ID
     * @param id The ID of the property configuration
     * @return The property configuration, if found
     */
    PropertyConfigurationDTO.Response getPropertyConfigurationById(Long id);
    
    /**
     * Get a property configuration by property ID
     * @param propertyId The ID of the property
     * @return The property configuration, if found
     */
    PropertyConfigurationDTO.Response getPropertyConfigurationByPropertyId(Integer propertyId);
    
    /**
     * Get all property configurations
     * @return List of all property configurations
     */
    List<PropertyConfigurationDTO.Response> getAllPropertyConfigurations();
    
    /**
     * Get property configurations by country
     * @param country The country to filter by
     * @return List of property configurations for the specified country
     */
    List<PropertyConfigurationDTO.Response> getPropertyConfigurationsByCountry(String country);
    
    /**
     * Delete a property configuration
     * @param id The ID of the property configuration to delete
     */
    void deletePropertyConfiguration(Long id);
    
    /**
     * Deactivate a property configuration
     * @param id The ID of the property configuration to deactivate
     * @return The deactivated property configuration
     */
    PropertyConfigurationDTO.Response deactivatePropertyConfiguration(Long id);
} 