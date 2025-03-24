package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, Long> {
    List<TenantConfiguration> findByTenantIdAndIsActive(Integer tenantId, Boolean isActive);
    List<TenantConfiguration> findByTenantIdAndPageAndIsActive(Integer tenantId, String page, Boolean isActive);
    Optional<TenantConfiguration> findByTenantIdAndPageAndFieldAndIsActive(Integer tenantId, String page, String field, Boolean isActive);
    List<TenantConfiguration> findByIsActive(Boolean isActive);
} 