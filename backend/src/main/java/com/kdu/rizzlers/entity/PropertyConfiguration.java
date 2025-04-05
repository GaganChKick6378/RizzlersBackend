package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "property_configuration")
public class PropertyConfiguration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "availability", nullable = false, columnDefinition = "TEXT")
    private String availability;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "surcharge", nullable = false, precision = 5, scale = 2)
    private BigDecimal surcharge;

    @Column(name = "fees", nullable = false, precision = 10, scale = 2)
    private BigDecimal fees;

    @Column(name = "terms_and_conditions", nullable = false, columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @Column(name = "tax", precision = 5, scale = 2)
    private BigDecimal tax;
} 