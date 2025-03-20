package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tenant_configuration")
public class TenantConfiguration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "page", nullable = false)
    private String page;

    @Column(name = "field", nullable = false)
    private String field;

    @Column(name = "value", nullable = false, columnDefinition = "jsonb")
    private String value; // JSONB stored as String

    @Column(name = "is_active")
    private Boolean isActive = true;
} 