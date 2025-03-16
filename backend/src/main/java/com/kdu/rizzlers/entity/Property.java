package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "properties")
public class Property extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Integer property_id;

    @Column(name = "property_name", nullable = false)
    private String property_name;

    @Column(name = "property_address")
    private String property_address;

    @Column(name = "contact_number")
    private String contact_number;

    @Column(name = "tenant_id")
    private Integer tenant_id;
}