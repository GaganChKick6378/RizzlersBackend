package com.kdu.rizzlers.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service for fetching and calculating room daily rates
 */
public interface RoomDailyRatesService {

    /**
     * Fetches the daily prices for a room type in the given date range
     * 
     * @param roomTypeId the room type ID
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @return a map of dates to prices
     */
    Map<LocalDate, Double> fetchDailyRoomTypePrices(
            Integer roomTypeId,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Calculates the total price for the stay
     * 
     * @param dailyPrices the map of daily prices
     * @param roomCount the number of rooms
     * @return the total price
     */
    double calculateTotalPrice(Map<LocalDate, Double> dailyPrices, int roomCount);
} 