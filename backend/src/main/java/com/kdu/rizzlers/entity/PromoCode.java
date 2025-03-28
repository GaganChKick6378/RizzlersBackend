package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_code_id")
    private Long promoCodeId;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "promo_code", nullable = false, unique = true, length = 50)
    private String promoCode;
    
    @Column(name = "price_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal priceFactor;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 