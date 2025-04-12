package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Shift entity
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    
    /**
     * Find all shifts for a property
     * 
     * @param propertyId the property ID
     * @return List of shifts for the property
     */
    List<Shift> findByPropertyId(Integer propertyId);
} 