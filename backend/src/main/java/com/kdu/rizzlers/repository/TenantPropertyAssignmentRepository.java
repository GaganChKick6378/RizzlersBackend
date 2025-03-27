package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.TenantPropertyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantPropertyAssignmentRepository extends JpaRepository<TenantPropertyAssignment, Long> {
    List<TenantPropertyAssignment> findByTenantId(Integer tenantId);
    List<TenantPropertyAssignment> findByTenantIdAndIsAssigned(Integer tenantId, Boolean isAssigned);
    List<TenantPropertyAssignment> findByPropertyId(Integer propertyId);
    Optional<TenantPropertyAssignment> findByTenantIdAndPropertyId(Integer tenantId, Integer propertyId);
} 