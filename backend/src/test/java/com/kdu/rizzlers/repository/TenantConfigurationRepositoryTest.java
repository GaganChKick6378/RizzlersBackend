package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.TenantConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TenantConfigurationRepositoryTest {

    @Autowired
    private TenantConfigurationRepository tenantConfigurationRepository;

    @Test
    @DisplayName("Should save and find tenant configuration")
    void saveAndFindTenantConfiguration() {
        // Arrange
        TenantConfiguration config = TenantConfiguration.builder()
                .tenantId(100)
                .page("homepage")
                .field("logo")
                .value("{\"url\": \"https://example.com/logo.png\"}")
                .isActive(true)
                .build();

        // Act
        TenantConfiguration savedConfig = tenantConfigurationRepository.save(config);
        Optional<TenantConfiguration> foundConfig = tenantConfigurationRepository.findById(savedConfig.getId());

        // Assert
        assertTrue(foundConfig.isPresent());
        assertEquals(100, foundConfig.get().getTenantId());
        assertEquals("homepage", foundConfig.get().getPage());
        assertEquals("logo", foundConfig.get().getField());
        assertEquals("{\"url\": \"https://example.com/logo.png\"}", foundConfig.get().getValue());
        assertTrue(foundConfig.get().getIsActive());
    }

    @Test
    @DisplayName("Should find configs by tenant ID and active status")
    @Sql("/sql/insert-test-tenant-configs.sql")
    void findByTenantIdAndIsActive() {
        // Arrange
        Integer tenantId = 100;
        Boolean isActive = true;

        // Act
        List<TenantConfiguration> configs = tenantConfigurationRepository.findByTenantIdAndIsActive(tenantId, isActive);

        // Assert
        assertFalse(configs.isEmpty());
        configs.forEach(config -> {
            assertEquals(tenantId, config.getTenantId());
            assertEquals(isActive, config.getIsActive());
        });
    }

    @Test
    @DisplayName("Should find configs by tenant ID, page, and active status")
    @Sql("/sql/insert-test-tenant-configs.sql")
    void findByTenantIdAndPageAndIsActive() {
        // Arrange
        Integer tenantId = 100;
        String page = "homepage";
        Boolean isActive = true;

        // Act
        List<TenantConfiguration> configs = tenantConfigurationRepository
                .findByTenantIdAndPageAndIsActive(tenantId, page, isActive);

        // Assert
        assertFalse(configs.isEmpty());
        configs.forEach(config -> {
            assertEquals(tenantId, config.getTenantId());
            assertEquals(page, config.getPage());
            assertEquals(isActive, config.getIsActive());
        });
    }

    @Test
    @DisplayName("Should find config by tenant ID, page, field, and active status")
    @Sql("/sql/insert-test-tenant-configs.sql")
    void findByTenantIdAndPageAndFieldAndIsActive() {
        // Arrange
        Integer tenantId = 100;
        String page = "homepage";
        String field = "logo";
        Boolean isActive = true;

        // Act
        Optional<TenantConfiguration> configOpt = tenantConfigurationRepository
                .findByTenantIdAndPageAndFieldAndIsActive(tenantId, page, field, isActive);

        // Assert
        assertTrue(configOpt.isPresent());
        TenantConfiguration config = configOpt.get();
        assertEquals(tenantId, config.getTenantId());
        assertEquals(page, config.getPage());
        assertEquals(field, config.getField());
        assertEquals(isActive, config.getIsActive());
    }

    @Test
    @DisplayName("Should not find config for non-existent tenant ID")
    @Sql("/sql/insert-test-tenant-configs.sql")
    void shouldNotFindConfigForNonExistentTenant() {
        // Arrange
        Integer nonExistentTenantId = 999;
        Boolean isActive = true;

        // Act
        List<TenantConfiguration> configs = tenantConfigurationRepository
                .findByTenantIdAndIsActive(nonExistentTenantId, isActive);

        // Assert
        assertTrue(configs.isEmpty());
    }

    @Test
    @DisplayName("Should find configs by active status")
    @Sql("/sql/insert-test-tenant-configs.sql")
    void findByIsActive() {
        // Arrange
        Boolean isActive = true;

        // Act
        List<TenantConfiguration> configs = tenantConfigurationRepository.findByIsActive(isActive);

        // Assert
        assertFalse(configs.isEmpty());
        configs.forEach(config -> 
            assertEquals(isActive, config.getIsActive())
        );
    }
} 