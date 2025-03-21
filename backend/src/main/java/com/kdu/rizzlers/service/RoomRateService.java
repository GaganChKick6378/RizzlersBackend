package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;

import java.time.LocalDate;
import java.util.List;

public interface RoomRateService {
    /**
     * Get all active promotions for a property during a specific date range
     * 
     * @param propertyId The property ID
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of property promotion schedules
     */
    List<PropertyPromotionSchedule> getActivePromotions(Integer propertyId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all promotions for a property
     * 
     * @param propertyId The property ID
     * @return List of all property promotion schedules
     */
    List<PropertyPromotionSchedule> getAllPromotions(Integer propertyId);
    
    /**
     * Get ALL minimum daily room rates for a property, including promotion information,
     * without filtering by date range
     * 
     * @param tenantId The tenant ID
     * @param propertyId The property ID
     * @return List of daily room rates with promotion information
     */
    List<DailyRoomRateDTO> getDailyRatesWithPromotions(Integer tenantId, Integer propertyId);
} 