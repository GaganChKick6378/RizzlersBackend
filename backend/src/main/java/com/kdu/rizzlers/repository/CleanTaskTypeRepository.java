package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.CleanTaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CleanTaskType entity
 */
@Repository
public interface CleanTaskTypeRepository extends JpaRepository<CleanTaskType, Integer> {
    
    /**
     * Find task type by name
     * 
     * @param typeName the task type name
     * @return Optional of CleanTaskType if found, otherwise empty
     */
    Optional<CleanTaskType> findByTypeName(String typeName);
} 