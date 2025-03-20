package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.PropertyPromotionScheduleRequest;
import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface PropertyPromotionScheduleService {
    PropertyPromotionScheduleResponse createSchedule(PropertyPromotionScheduleRequest request);
    PropertyPromotionScheduleResponse getScheduleById(Long id);
    List<PropertyPromotionScheduleResponse> getAllSchedules();
    List<PropertyPromotionScheduleResponse> getSchedulesByPropertyId(Integer propertyId);
    List<PropertyPromotionScheduleResponse> getSchedulesByPromotionId(Integer promotionId);
    List<PropertyPromotionScheduleResponse> getActivePromotionsForPropertyBetweenDates(
            Integer propertyId, LocalDate startDate, LocalDate endDate);
    PropertyPromotionScheduleResponse updateSchedule(Long id, PropertyPromotionScheduleRequest request);
    void deleteSchedule(Long id);
} 