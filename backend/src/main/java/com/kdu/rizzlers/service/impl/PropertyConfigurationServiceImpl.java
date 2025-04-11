package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.PropertyConfigurationDTO;
import com.kdu.rizzlers.entity.PropertyConfiguration;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.PropertyConfigurationRepository;
import com.kdu.rizzlers.service.PropertyConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyConfigurationServiceImpl implements PropertyConfigurationService {

    private final PropertyConfigurationRepository propertyConfigurationRepository;

    @Override
    @Transactional
    public PropertyConfigurationDTO.Response createPropertyConfiguration(PropertyConfigurationDTO.Request request) {
        log.info("Creating new property configuration for property ID: {}", request.getPropertyId());
        
        // Check if configuration already exists for this property
        propertyConfigurationRepository.findByPropertyIdAndIsActiveTrue(request.getPropertyId())
                .ifPresent(existing -> {
                    log.warn("Active configuration already exists for property ID: {}", request.getPropertyId());
                    throw new IllegalStateException("An active configuration already exists for this property. Please update the existing one.");
                });
        
        PropertyConfiguration entity = request.toEntity();
        entity = propertyConfigurationRepository.save(entity);
        log.info("Created property configuration with ID: {}", entity.getId());
        
        return PropertyConfigurationDTO.Response.fromEntity(entity);
    }

    @Override
    @Transactional
    public PropertyConfigurationDTO.Response updatePropertyConfiguration(Long id, PropertyConfigurationDTO.Request request) {
        log.info("Updating property configuration with ID: {}", id);
        
        PropertyConfiguration existingConfig = propertyConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property configuration not found with ID: " + id));
        
        // Update fields from request
        existingConfig.setPropertyId(request.getPropertyId());
        existingConfig.setContactNumber(request.getContactNumber());
        existingConfig.setAvailability(request.getAvailability());
        existingConfig.setCountry(request.getCountry());
        existingConfig.setSurcharge(request.getSurcharge());
        existingConfig.setFees(request.getFees());
        existingConfig.setTermsAndConditions(request.getTermsAndConditions());
        
        existingConfig = propertyConfigurationRepository.save(existingConfig);
        log.info("Updated property configuration with ID: {}", existingConfig.getId());
        
        return PropertyConfigurationDTO.Response.fromEntity(existingConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyConfigurationDTO.Response getPropertyConfigurationById(Long id) {
        log.info("Fetching property configuration with ID: {}", id);
        
        PropertyConfiguration config = propertyConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property configuration not found with ID: " + id));
        
        return PropertyConfigurationDTO.Response.fromEntity(config);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyConfigurationDTO.Response getPropertyConfigurationByPropertyId(Integer propertyId) {
        log.info("Fetching property configuration for property ID: {}", propertyId);
        
        PropertyConfiguration config = propertyConfigurationRepository.findByPropertyIdAndIsActiveTrue(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property configuration not found for property ID: " + propertyId));
        
        return PropertyConfigurationDTO.Response.fromEntity(config);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyConfigurationDTO.Response> getAllPropertyConfigurations() {
        log.info("Fetching all property configurations");
        
        return propertyConfigurationRepository.findByIsActiveTrue().stream()
                .map(PropertyConfigurationDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyConfigurationDTO.Response> getPropertyConfigurationsByCountry(String country) {
        log.info("Fetching property configurations for country: {}", country);
        
        return propertyConfigurationRepository.findByCountry(country).stream()
                .filter(PropertyConfiguration::getIsActive)
                .map(PropertyConfigurationDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePropertyConfiguration(Long id) {
        log.info("Deleting property configuration with ID: {}", id);
        
        PropertyConfiguration config = propertyConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property configuration not found with ID: " + id));
        
        propertyConfigurationRepository.delete(config);
        log.info("Deleted property configuration with ID: {}", id);
    }

    @Override
    @Transactional
    public PropertyConfigurationDTO.Response deactivatePropertyConfiguration(Long id) {
        log.info("Deactivating property configuration with ID: {}", id);
        
        PropertyConfiguration config = propertyConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property configuration not found with ID: " + id));
        
        config.setIsActive(false);
        config = propertyConfigurationRepository.save(config);
        log.info("Deactivated property configuration with ID: {}", id);
        
        return PropertyConfigurationDTO.Response.fromEntity(config);
    }
} 