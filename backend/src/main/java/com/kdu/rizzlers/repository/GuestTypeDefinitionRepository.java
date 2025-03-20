package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.GuestTypeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestTypeDefinitionRepository extends JpaRepository<GuestTypeDefinition, Long> {
    List<GuestTypeDefinition> findByTenantIdAndIsActive(Integer tenantId, Boolean isActive);
    Optional<GuestTypeDefinition> findByTenantIdAndGuestTypeAndIsActive(Integer tenantId, String guestType, Boolean isActive);
    List<GuestTypeDefinition> findByTenantId(Integer tenantId);
} 