package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PropertyPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PropertyPreferences entity
 */
@Repository
public interface PropertyPreferencesRepository extends JpaRepository<PropertyPreferences, Integer> {
    
    /**
     * Find property preferences by property ID
     * 
     * @param propertyId the property ID
     * @return Optional of PropertyPreferences if found, otherwise empty
     */
    Optional<PropertyPreferences> findByPropertyId(Integer propertyId);
} 