package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PropertyConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyConfigurationRepository extends JpaRepository<PropertyConfiguration, Long> {
    
    // Find by property ID
    Optional<PropertyConfiguration> findByPropertyId(Integer propertyId);
    
    // Find by property ID and active status
    Optional<PropertyConfiguration> findByPropertyIdAndIsActiveTrue(Integer propertyId);
    
    // Find by country
    List<PropertyConfiguration> findByCountry(String country);
    
    // Find all active configurations
    List<PropertyConfiguration> findByIsActiveTrue();
} 