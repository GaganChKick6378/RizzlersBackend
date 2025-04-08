package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.PropertyConfigurationDTO;
import com.kdu.rizzlers.entity.PropertyConfiguration;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.PropertyConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyConfigurationServiceImplTest {

    @Mock
    private PropertyConfigurationRepository propertyConfigurationRepository;

    @InjectMocks
    private PropertyConfigurationServiceImpl propertyConfigurationService;

    private PropertyConfiguration sampleConfig;
    private PropertyConfigurationDTO.Request sampleRequest;

    @BeforeEach
    void setUp() {
        sampleConfig = new PropertyConfiguration();
        sampleConfig.setId(1L);
        sampleConfig.setPropertyId(100);
        sampleConfig.setContactNumber("+12345678900");
        sampleConfig.setCountry("United States");
        sampleConfig.setIsActive(true);
        sampleConfig.setSurcharge(new BigDecimal("5.0"));
        sampleConfig.setFees(new BigDecimal("10.0"));
        sampleConfig.setTermsAndConditions("Sample terms and conditions");
        
        sampleRequest = PropertyConfigurationDTO.Request.builder()
                .propertyId(100)
                .contactNumber("+12345678900")
                .country("United States")
                .surcharge(new BigDecimal("5.0"))
                .fees(new BigDecimal("10.0"))
                .termsAndConditions("Sample terms and conditions")
                .availability("true")
                .build();
    }

    @Test
    void createPropertyConfiguration_Success() {
        // Arrange
        when(propertyConfigurationRepository.findByPropertyIdAndIsActiveTrue(anyInt()))
                .thenReturn(Optional.empty());
        
        PropertyConfiguration savedEntity = new PropertyConfiguration();
        savedEntity.setId(1L);
        savedEntity.setPropertyId(sampleRequest.getPropertyId());
        savedEntity.setContactNumber(sampleRequest.getContactNumber());
        savedEntity.setCountry(sampleRequest.getCountry());
        savedEntity.setSurcharge(sampleRequest.getSurcharge());
        savedEntity.setFees(sampleRequest.getFees());
        savedEntity.setTermsAndConditions(sampleRequest.getTermsAndConditions());
        savedEntity.setAvailability(sampleRequest.getAvailability());
        savedEntity.setIsActive(true);
        
        when(propertyConfigurationRepository.save(any(PropertyConfiguration.class)))
                .thenReturn(savedEntity);
        
        // Act
        PropertyConfigurationDTO.Response result = propertyConfigurationService.createPropertyConfiguration(sampleRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(sampleRequest.getPropertyId(), result.getPropertyId());
        
        verify(propertyConfigurationRepository).findByPropertyIdAndIsActiveTrue(sampleRequest.getPropertyId());
        verify(propertyConfigurationRepository).save(any(PropertyConfiguration.class));
    }

    @Test
    void createPropertyConfiguration_AlreadyExists_ThrowsException() {
        // Arrange
        when(propertyConfigurationRepository.findByPropertyIdAndIsActiveTrue(anyInt()))
                .thenReturn(Optional.of(sampleConfig));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            propertyConfigurationService.createPropertyConfiguration(sampleRequest);
        });
        
        verify(propertyConfigurationRepository).findByPropertyIdAndIsActiveTrue(sampleRequest.getPropertyId());
        verify(propertyConfigurationRepository, never()).save(any(PropertyConfiguration.class));
    }

    @Test
    void updatePropertyConfiguration_Success() {
        // Arrange
        Long configId = 1L;
        PropertyConfigurationDTO.Request updateRequest = PropertyConfigurationDTO.Request.builder()
                .propertyId(101)
                .contactNumber("+19876543210")
                .country("Canada")
                .surcharge(new BigDecimal("7.5"))
                .fees(new BigDecimal("12.5"))
                .termsAndConditions("Updated terms")
                .availability("false")
                .build();
        
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.of(sampleConfig));
        
        PropertyConfiguration updatedEntity = new PropertyConfiguration();
        updatedEntity.setId(configId);
        updatedEntity.setPropertyId(updateRequest.getPropertyId());
        updatedEntity.setContactNumber(updateRequest.getContactNumber());
        updatedEntity.setCountry(updateRequest.getCountry());
        updatedEntity.setSurcharge(updateRequest.getSurcharge());
        updatedEntity.setFees(updateRequest.getFees());
        updatedEntity.setTermsAndConditions(updateRequest.getTermsAndConditions());
        updatedEntity.setAvailability(updateRequest.getAvailability());
        updatedEntity.setIsActive(true);
        
        when(propertyConfigurationRepository.save(any(PropertyConfiguration.class)))
                .thenReturn(updatedEntity);
        
        // Act
        PropertyConfigurationDTO.Response result = propertyConfigurationService.updatePropertyConfiguration(configId, updateRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(configId, result.getId());
        assertEquals(updateRequest.getPropertyId(), result.getPropertyId());
        assertEquals(updateRequest.getContactNumber(), result.getContactNumber());
        assertEquals(updateRequest.getCountry(), result.getCountry());
        
        ArgumentCaptor<PropertyConfiguration> configCaptor = ArgumentCaptor.forClass(PropertyConfiguration.class);
        verify(propertyConfigurationRepository).save(configCaptor.capture());
        
        PropertyConfiguration capturedConfig = configCaptor.getValue();
        assertEquals(updateRequest.getPropertyId(), capturedConfig.getPropertyId());
        assertEquals(updateRequest.getContactNumber(), capturedConfig.getContactNumber());
        assertEquals(updateRequest.getCountry(), capturedConfig.getCountry());
        assertEquals(updateRequest.getSurcharge(), capturedConfig.getSurcharge());
        assertEquals(updateRequest.getFees(), capturedConfig.getFees());
        assertEquals(updateRequest.getTermsAndConditions(), capturedConfig.getTermsAndConditions());
        assertEquals(updateRequest.getAvailability(), capturedConfig.getAvailability());
    }

    @Test
    void updatePropertyConfiguration_NotFound_ThrowsException() {
        // Arrange
        Long configId = 999L;
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            propertyConfigurationService.updatePropertyConfiguration(configId, sampleRequest);
        });
        
        verify(propertyConfigurationRepository).findById(configId);
        verify(propertyConfigurationRepository, never()).save(any(PropertyConfiguration.class));
    }

    @Test
    void getPropertyConfigurationById_Success() {
        // Arrange
        Long configId = 1L;
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.of(sampleConfig));
        
        // Act
        PropertyConfigurationDTO.Response result = propertyConfigurationService.getPropertyConfigurationById(configId);
        
        // Assert
        assertNotNull(result);
        assertEquals(sampleConfig.getId(), result.getId());
        assertEquals(sampleConfig.getPropertyId(), result.getPropertyId());
        assertEquals(sampleConfig.getContactNumber(), result.getContactNumber());
        
        verify(propertyConfigurationRepository).findById(configId);
    }

    @Test
    void getPropertyConfigurationById_NotFound_ThrowsException() {
        // Arrange
        Long configId = 999L;
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            propertyConfigurationService.getPropertyConfigurationById(configId);
        });
        
        verify(propertyConfigurationRepository).findById(configId);
    }

    @Test
    void getPropertyConfigurationByPropertyId_Success() {
        // Arrange
        Integer propertyId = 100;
        when(propertyConfigurationRepository.findByPropertyIdAndIsActiveTrue(propertyId))
                .thenReturn(Optional.of(sampleConfig));
        
        // Act
        PropertyConfigurationDTO.Response result = propertyConfigurationService.getPropertyConfigurationByPropertyId(propertyId);
        
        // Assert
        assertNotNull(result);
        assertEquals(sampleConfig.getId(), result.getId());
        assertEquals(sampleConfig.getPropertyId(), result.getPropertyId());
        
        verify(propertyConfigurationRepository).findByPropertyIdAndIsActiveTrue(propertyId);
    }

    @Test
    void getPropertyConfigurationByPropertyId_NotFound_ThrowsException() {
        // Arrange
        Integer propertyId = 999;
        when(propertyConfigurationRepository.findByPropertyIdAndIsActiveTrue(propertyId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            propertyConfigurationService.getPropertyConfigurationByPropertyId(propertyId);
        });
        
        verify(propertyConfigurationRepository).findByPropertyIdAndIsActiveTrue(propertyId);
    }

    @Test
    void getAllPropertyConfigurations_Success() {
        // Arrange
        PropertyConfiguration config1 = new PropertyConfiguration();
        config1.setId(1L);
        config1.setPropertyId(100);
        config1.setIsActive(true);
        
        PropertyConfiguration config2 = new PropertyConfiguration();
        config2.setId(2L);
        config2.setPropertyId(200);
        config2.setIsActive(true);
        
        List<PropertyConfiguration> configs = Arrays.asList(config1, config2);
        
        when(propertyConfigurationRepository.findByIsActiveTrue())
                .thenReturn(configs);
        
        // Act
        List<PropertyConfigurationDTO.Response> results = propertyConfigurationService.getAllPropertyConfigurations();
        
        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
        
        verify(propertyConfigurationRepository).findByIsActiveTrue();
    }

    @Test
    void getPropertyConfigurationsByCountry_Success() {
        // Arrange
        String country = "United States";
        
        PropertyConfiguration config1 = new PropertyConfiguration();
        config1.setId(1L);
        config1.setPropertyId(100);
        config1.setCountry(country);
        config1.setIsActive(true);
        
        PropertyConfiguration config2 = new PropertyConfiguration();
        config2.setId(2L);
        config2.setPropertyId(200);
        config2.setCountry(country);
        config2.setIsActive(true);
        
        PropertyConfiguration inactiveConfig = new PropertyConfiguration();
        inactiveConfig.setId(3L);
        inactiveConfig.setPropertyId(300);
        inactiveConfig.setCountry(country);
        inactiveConfig.setIsActive(false);
        
        List<PropertyConfiguration> configs = Arrays.asList(config1, config2, inactiveConfig);
        
        when(propertyConfigurationRepository.findByCountry(country))
                .thenReturn(configs);
        
        // Act
        List<PropertyConfigurationDTO.Response> results = propertyConfigurationService.getPropertyConfigurationsByCountry(country);
        
        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // Only active configurations
        
        verify(propertyConfigurationRepository).findByCountry(country);
    }

    @Test
    void deletePropertyConfiguration_Success() {
        // Arrange
        Long configId = 1L;
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.of(sampleConfig));
        
        // Act
        propertyConfigurationService.deletePropertyConfiguration(configId);
        
        // Assert
        verify(propertyConfigurationRepository).findById(configId);
        verify(propertyConfigurationRepository).delete(sampleConfig);
    }

    @Test
    void deletePropertyConfiguration_NotFound_ThrowsException() {
        // Arrange
        Long configId = 999L;
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            propertyConfigurationService.deletePropertyConfiguration(configId);
        });
        
        verify(propertyConfigurationRepository).findById(configId);
        verify(propertyConfigurationRepository, never()).delete(any(PropertyConfiguration.class));
    }

    @Test
    void deactivatePropertyConfiguration_Success() {
        // Arrange
        Long configId = 1L;
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.of(sampleConfig));
        
        PropertyConfiguration deactivatedConfig = new PropertyConfiguration();
        deactivatedConfig.setId(configId);
        deactivatedConfig.setPropertyId(sampleConfig.getPropertyId());
        deactivatedConfig.setIsActive(false);
        
        when(propertyConfigurationRepository.save(any(PropertyConfiguration.class)))
                .thenReturn(deactivatedConfig);
        
        // Act
        PropertyConfigurationDTO.Response result = propertyConfigurationService.deactivatePropertyConfiguration(configId);
        
        // Assert
        assertNotNull(result);
        assertEquals(configId, result.getId());
        assertFalse(result.getIsActive());
        
        ArgumentCaptor<PropertyConfiguration> configCaptor = ArgumentCaptor.forClass(PropertyConfiguration.class);
        verify(propertyConfigurationRepository).save(configCaptor.capture());
        
        PropertyConfiguration capturedConfig = configCaptor.getValue();
        assertFalse(capturedConfig.getIsActive());
    }

    @Test
    void deactivatePropertyConfiguration_NotFound_ThrowsException() {
        // Arrange
        Long configId = 999L;
        when(propertyConfigurationRepository.findById(configId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            propertyConfigurationService.deactivatePropertyConfiguration(configId);
        });
        
        verify(propertyConfigurationRepository).findById(configId);
        verify(propertyConfigurationRepository, never()).save(any(PropertyConfiguration.class));
    }
} 