package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PropertyPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PropertyPromotionRepository extends JpaRepository<PropertyPromotion, Long> {

    /**
     * Find active property promotions that overlap with the given date range
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of property promotions that are active and overlap with the date range
     */
    @Query("SELECT pp FROM PropertyPromotion pp WHERE pp.isActive = true " +
           "AND ((pp.startDate <= :endDate) AND (pp.endDate >= :startDate))")
    List<PropertyPromotion> findActivePromotionsInDateRange(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * Find active and visible property promotions that overlap with the given date range
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of property promotions that are active, visible, and overlap with the date range
     */
    @Query("SELECT pp FROM PropertyPromotion pp WHERE pp.isActive = true " +
           "AND pp.isVisible = true " +
           "AND ((pp.startDate <= :endDate) AND (pp.endDate >= :startDate))")
    List<PropertyPromotion> findActiveAndVisiblePromotionsInDateRange(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
            
    /**
     * Find active property promotions for a specific property that overlap with the given date range
     * 
     * @param propertyId The property ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of property promotions that are active and overlap with the date range for the given property
     */
    @Query("SELECT pp FROM PropertyPromotion pp WHERE pp.isActive = true " +
           "AND pp.propertyId = :propertyId " +
           "AND ((pp.startDate <= :endDate) AND (pp.endDate >= :startDate))")
    List<PropertyPromotion> findActivePromotionsForPropertyInDateRange(
            @Param("propertyId") Integer propertyId,
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
            
    /**
     * Find active and visible property promotions for a specific property that overlap with the given date range
     * 
     * @param propertyId The property ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of property promotions that are active, visible, and overlap with the date range for the given property
     */
    @Query("SELECT pp FROM PropertyPromotion pp WHERE pp.isActive = true " +
           "AND pp.isVisible = true " +
           "AND pp.propertyId = :propertyId " +
           "AND ((pp.startDate <= :endDate) AND (pp.endDate >= :startDate))")
    List<PropertyPromotion> findActiveAndVisiblePromotionsForPropertyInDateRange(
            @Param("propertyId") Integer propertyId,
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
} 