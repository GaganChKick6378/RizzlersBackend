package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.PropertyResponse;
import com.kdu.rizzlers.dto.out.ResultsPageConfigResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.dto.out.TenantPropertyAssignmentResponse;
import com.kdu.rizzlers.entity.TenantConfiguration;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.TenantConfigurationRepository;
import com.kdu.rizzlers.service.GuestTypeDefinitionService;
import com.kdu.rizzlers.service.TenantConfigurationService;
import com.kdu.rizzlers.service.FilterOptionsService;
import com.kdu.rizzlers.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of the TenantConfigurationService.
 * This class has been refactored to use helper classes for specific functionalities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantConfigurationServiceImpl implements TenantConfigurationService {

    private final TenantConfigurationRepository tenantConfigurationRepository;
    private final GuestTypeDefinitionService guestTypeDefinitionService;
    private final PropertyServiceHelper propertyServiceHelper;
    private final ConfigurationValidator configValidator;
    private final ConfigurationDefaultProvider defaultProvider;
    private final FilterOptionsService filterOptionsService;

    @Override
    @Transactional
    public TenantConfigurationResponse createConfiguration(TenantConfigurationRequest request) {
        TenantConfiguration configuration = TenantConfiguration.builder()
                .tenantId(request.getTenantId())
                .page(request.getPage())
                .field(request.getField())
                .value(request.getValue())
                .isActive(request.getIsActive())
                .build();
        
        TenantConfiguration savedConfiguration = tenantConfigurationRepository.save(configuration);
        return mapToResponse(savedConfiguration);
    }

    @Override
    public TenantConfigurationResponse getConfigurationById(Long id) {
        TenantConfiguration configuration = tenantConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "id", id));
        return mapToResponse(configuration);
    }

    @Override
    public List<TenantConfigurationResponse> getAllConfigurations() {
        return tenantConfigurationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantConfigurationResponse> getConfigurationsByTenantId(Integer tenantId) {
        return tenantConfigurationRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantConfigurationResponse> getConfigurationsByTenantIdAndPage(Integer tenantId, String page) {
        return tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(tenantId, page, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TenantConfigurationResponse getConfigurationByTenantIdAndPageAndField(Integer tenantId, String page, String field) {
        TenantConfiguration configuration = tenantConfigurationRepository.findByTenantIdAndPageAndFieldAndIsActive(tenantId, page, field, true)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "tenantId, page, field", tenantId + ", " + page + ", " + field));
        return mapToResponse(configuration);
    }

    @Override
    @Transactional
    public TenantConfigurationResponse updateConfiguration(Long id, TenantConfigurationRequest request) {
        TenantConfiguration configuration = tenantConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "id", id));
        
        configuration.setTenantId(request.getTenantId());
        configuration.setPage(request.getPage());
        configuration.setField(request.getField());
        configuration.setValue(request.getValue());
        configuration.setIsActive(request.getIsActive());
        
        TenantConfiguration updatedConfiguration = tenantConfigurationRepository.save(configuration);
        return mapToResponse(updatedConfiguration);
    }

    @Override
    @Transactional
    public void deleteConfiguration(Long id) {
        TenantConfiguration configuration = tenantConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "id", id));
        
        // Soft delete: update isActive to false
        configuration.setIsActive(false);
        tenantConfigurationRepository.save(configuration);
    }
    
    @Override
    public LandingPageConfigResponse getLandingPageConfiguration(Integer tenantId, boolean fetchPropertyDetails) {
        log.info("Getting landing page configuration for tenant: {}, fetchPropertyDetails: {}", tenantId, fetchPropertyDetails);
        
        // Get configurations from the database
        List<TenantConfiguration> configurations = tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(
                tenantId, "landing", true);
        
        log.debug("Found {} active configurations for tenant: {}", configurations.size(), tenantId);
        
        // Get guest type definitions
        List<GuestTypeDefinitionResponse> guestTypes = guestTypeDefinitionService.getGuestTypeDefinitionsByTenantId(tenantId);
        
        // Create response builder (without property details initially)
        LandingPageConfigResponse.LandingPageConfigResponseBuilder builder = LandingPageConfigResponse.builder()
                .tenantId(tenantId)
                .page("landing")
                .guestTypes(guestTypes);
        
        // Set default values for all fields to ensure no null values in the response
        defaultProvider.setDefaultConfigValues(builder);
        
        // Set property assignments
        List<TenantPropertyAssignmentResponse> propertyResponses = propertyServiceHelper.getPropertyAssignments(tenantId, fetchPropertyDetails);
        builder.properties(propertyResponses);
        
        // Map each configuration to the corresponding field in the response
        processConfigurations(configurations, builder);
        
        return builder.build();
    }
    
    @Override
    public LandingPageConfigResponse getLandingPageConfiguration(Integer tenantId) {
        return getLandingPageConfiguration(tenantId, true);
    }
    
    /**
     * Process each configuration and apply it to the builder if valid
     * 
     * @param configurations List of tenant configurations
     * @param builder The response builder to update
     */
    private void processConfigurations(List<TenantConfiguration> configurations, 
                                      LandingPageConfigResponse.LandingPageConfigResponseBuilder builder) {
        for (TenantConfiguration config : configurations) {
            String field = config.getField();
            Map<String, Object> valueMap;
            
            try {
                valueMap = JsonUtil.jsonToMap(config.getValue());
                if (valueMap == null || valueMap.isEmpty()) {
                    log.warn("Empty or invalid JSON for field: {}, tenant: {}", field, config.getTenantId());
                    continue; // Skip this field, use default value
                }
            } catch (Exception e) {
                log.error("Error parsing JSON for field: {}, tenant: {}, error: {}", 
                          field, config.getTenantId(), e.getMessage());
                continue; // Skip this field, use default value
            }
            
            // Validate and set each field
            try {
                applyConfigurationField(field, valueMap, builder);
            } catch (Exception e) {
                log.error("Error validating field: {}, tenant: {}, error: {}", 
                          field, config.getTenantId(), e.getMessage());
                // Continue with the next field, keeping the default value for this one
            }
        }
    }
    
    /**
     * Apply a single configuration field to the builder if valid
     * 
     * @param field The configuration field name
     * @param valueMap The configuration value as a map
     * @param builder The response builder to update
     */
    private void applyConfigurationField(String field, Map<String, Object> valueMap, 
                                        LandingPageConfigResponse.LandingPageConfigResponseBuilder builder) {
        switch (field) {
            case "header_logo":
                if (configValidator.validateHeaderLogo(valueMap)) {
                    builder.headerLogo(valueMap);
                }
                break;
            case "page_title":
                if (configValidator.validatePageTitle(valueMap)) {
                    builder.pageTitle(valueMap);
                }
                break;
            case "banner_image":
                if (configValidator.validateBannerImage(valueMap)) {
                    builder.bannerImage(valueMap);
                }
                break;
            case "footer":
                if (configValidator.validateFooter(valueMap)) {
                    builder.footer(valueMap);
                }
                break;
            case "languages":
                if (configValidator.validateLanguages(valueMap)) {
                    builder.languages(valueMap);
                }
                break;
            case "currencies":
                if (configValidator.validateCurrencies(valueMap)) {
                    builder.currencies(valueMap);
                }
                break;
            case "length_of_stay":
                if (configValidator.validateLengthOfStay(valueMap)) {
                    builder.lengthOfStay(valueMap);
                }
                break;
            case "guest_options":
                if (configValidator.validateGuestOptions(valueMap)) {
                    builder.guestOptions(valueMap);
                }
                break;
            case "room_options":
                if (configValidator.validateRoomOptions(valueMap)) {
                    builder.roomOptions(valueMap);
                }
                break;
            case "accessibility_options":
                if (configValidator.validateAccessibilityOptions(valueMap)) {
                    builder.accessibilityOptions(valueMap);
                }
                break;
            case "number_of_rooms":
                if (configValidator.validateNumberOfRooms(valueMap)) {
                    builder.numberOfRooms(valueMap);
                }
                break;
            default:
                log.warn("Unknown configuration field: {}", field);
                break;
        }
    }

    @Override
    public ResultsPageConfigResponse getResultsPageConfiguration(Integer tenantId, boolean fetchPropertyDetails) {
        log.info("Getting results page configuration for tenant: {}, fetchPropertyDetails: {}", tenantId, fetchPropertyDetails);
        
        // Get configurations from the database
        List<TenantConfiguration> configurations = tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(
                tenantId, "results", true);
        
        log.debug("Found {} active configurations for tenant: {}", configurations.size(), tenantId);
        
        // Get guest type definitions
        List<GuestTypeDefinitionResponse> guestTypes = guestTypeDefinitionService.getGuestTypeDefinitionsByTenantId(tenantId);
        
        // Create response builder (without property details initially)
        ResultsPageConfigResponse.ResultsPageConfigResponseBuilder builder = ResultsPageConfigResponse.builder()
                .tenantId(tenantId)
                .page("results")
                .guestTypes(guestTypes);
        
        // Set default values for all fields to ensure no null values in the response
        defaultProvider.setResultsPageDefaultValues(builder);
        
        // Set property assignments
        List<TenantPropertyAssignmentResponse> propertyResponses = propertyServiceHelper.getPropertyAssignments(tenantId, fetchPropertyDetails);
        builder.properties(propertyResponses);
        
        // Process each configuration and apply it to the results page builder
        processResultsPageConfigurations(configurations, builder);
        
        // Build the initial response to get the filter map
        ResultsPageConfigResponse response = builder.build();
        
        // Update filters with dynamic data
        Map<String, Object> updatedFilters = filterOptionsService.updateFiltersWithDynamicData(response.getFilters());
        
        // Set the updated filters back to the builder
        builder.filters(updatedFilters);
        
        return builder.build();
    }
    
    @Override
    public ResultsPageConfigResponse getResultsPageConfiguration(Integer tenantId) {
        return getResultsPageConfiguration(tenantId, true);
    }
    
    /**
     * Process each configuration and apply it to the builder if valid for results page
     * 
     * @param configurations List of tenant configurations
     * @param builder The response builder to update
     */
    private void processResultsPageConfigurations(List<TenantConfiguration> configurations, 
                                               ResultsPageConfigResponse.ResultsPageConfigResponseBuilder builder) {
        for (TenantConfiguration config : configurations) {
            String field = config.getField();
            Map<String, Object> valueMap;
            
            try {
                valueMap = JsonUtil.jsonToMap(config.getValue());
                if (valueMap == null || valueMap.isEmpty()) {
                    log.warn("Empty or invalid JSON for field: {}, tenant: {}", field, config.getTenantId());
                    continue; // Skip this field, use default value
                }
            } catch (Exception e) {
                log.error("Error parsing JSON for field: {}, tenant: {}, error: {}", 
                          field, config.getTenantId(), e.getMessage());
                continue; // Skip this field, use default value
            }
            
            // Validate and set each field
            try {
                applyResultsPageConfigurationField(field, valueMap, builder);
            } catch (Exception e) {
                log.error("Error validating field: {}, tenant: {}, error: {}", 
                          field, config.getTenantId(), e.getMessage());
                // Continue with the next field, keeping the default value for this one
            }
        }
    }
    
    /**
     * Apply a single configuration field to the builder if valid for results page
     * 
     * @param field The configuration field name
     * @param valueMap The configuration value as a map
     * @param builder The response builder to update
     */
    private void applyResultsPageConfigurationField(String field, Map<String, Object> valueMap, 
                                                 ResultsPageConfigResponse.ResultsPageConfigResponseBuilder builder) {
        switch (field) {
            case "filters":
                if (configValidator.validateFilters(valueMap)) {
                    builder.filters(valueMap);
                }
                break;
            case "sorting":
                if (configValidator.validateSorting(valueMap)) {
                    builder.sorting(valueMap);
                }
                break;
            case "pagination":
                if (configValidator.validatePagination(valueMap)) {
                    builder.pagination(valueMap);
                }
                break;
            case "display_options":
                if (configValidator.validateDisplayOptions(valueMap)) {
                    builder.displayOptions(valueMap);
                }
                break;
            // For backward compatibility with the older filter_options and sort_options fields
            case "filter_options":
                log.info("Processing legacy field 'filter_options', consider migrating to 'filters'");
                Map<String, Object> newFilterMap = new HashMap<>(builder.build().getFilters());
                // Apply the filter options to the existing filter map 
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    newFilterMap.put(entry.getKey(), entry.getValue());
                }
                builder.filters(newFilterMap);
                break;
            case "sort_options":
                log.info("Processing legacy field 'sort_options', consider migrating to 'sorting'");
                Map<String, Object> newSortingMap = new HashMap<>(builder.build().getSorting());
                // Apply the sort options to the existing sorting map
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    newSortingMap.put(entry.getKey(), entry.getValue());
                }
                builder.sorting(newSortingMap);
                break;
            default:
                log.warn("Unknown results page configuration field: {}", field);
                break;
        }
    }

    private TenantConfigurationResponse mapToResponse(TenantConfiguration configuration) {
        return TenantConfigurationResponse.builder()
                .id(configuration.getId())
                .tenantId(configuration.getTenantId())
                .page(configuration.getPage())
                .field(configuration.getField())
                .value(configuration.getValue())
                .isActive(configuration.getIsActive())
                .createdAt(configuration.getCreatedAt())
                .updatedAt(configuration.getUpdatedAt())
                .build();
    }    
} 