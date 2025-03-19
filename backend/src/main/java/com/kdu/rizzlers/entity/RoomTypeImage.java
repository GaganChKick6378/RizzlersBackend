package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_type_images")
public class RoomTypeImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "room_type_id", nullable = false)
    private Integer roomTypeId;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "image_urls", nullable = false, columnDefinition = "varchar(512)[]")
    private String[] imageUrls;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
} 