package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PromoCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PromoCodeRepositoryTest {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Test
    @DisplayName("Should save and find promo code")
    void saveAndFindPromoCode() {
        // Arrange
        PromoCode promoCode = PromoCode.builder()
                .title("Test Promo")
                .description("Test Description")
                .promoCode("TEST123")
                .priceFactor(new BigDecimal("0.9"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);
        Optional<PromoCode> foundPromoCode = promoCodeRepository.findById(savedPromoCode.getPromoCodeId());

        // Assert
        assertTrue(foundPromoCode.isPresent());
        assertEquals("TEST123", foundPromoCode.get().getPromoCode());
        assertEquals("Test Promo", foundPromoCode.get().getTitle());
        assertEquals(new BigDecimal("0.9"), foundPromoCode.get().getPriceFactor());
        assertTrue(foundPromoCode.get().getIsAvailable());
    }

    @Test
    @DisplayName("Should find promo code by code")
    @Sql("/sql/insert-test-promo-codes.sql")
    void findByPromoCode() {
        // Arrange
        String code = "SUMMER10";

        // Act
        Optional<PromoCode> foundPromoCode = promoCodeRepository.findByPromoCode(code);

        // Assert
        assertTrue(foundPromoCode.isPresent());
        assertEquals(code, foundPromoCode.get().getPromoCode());
    }

    @Test
    @DisplayName("Should find valid promo code by code and date range")
    @Sql("/sql/insert-test-promo-codes.sql")
    void findByPromoCodeAndIsAvailableTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual() {
        // Arrange
        String code = "SUMMER10";
        LocalDate currentDate = LocalDate.now();

        // Act
        Optional<PromoCode> foundPromoCode = promoCodeRepository
                .findByPromoCodeAndIsAvailableTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        code, currentDate, currentDate);

        // Assert
        assertTrue(foundPromoCode.isPresent());
        assertEquals(code, foundPromoCode.get().getPromoCode());
        assertTrue(foundPromoCode.get().getIsAvailable());
        assertTrue(currentDate.compareTo(foundPromoCode.get().getStartDate()) >= 0); // currentDate >= startDate
        assertTrue(currentDate.compareTo(foundPromoCode.get().getEndDate()) <= 0);   // currentDate <= endDate
    }

    @Test
    @DisplayName("Should not find expired promo code")
    @Sql("/sql/insert-test-promo-codes.sql")
    void shouldNotFindExpiredPromoCode() {
        // Arrange
        String code = "EXPIRED10";
        LocalDate currentDate = LocalDate.now();

        // Act
        Optional<PromoCode> foundPromoCode = promoCodeRepository
                .findByPromoCodeAndIsAvailableTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        code, currentDate, currentDate);

        // Assert
        assertFalse(foundPromoCode.isPresent());
    }

    @Test
    @DisplayName("Should not find inactive promo code")
    @Sql("/sql/insert-test-promo-codes.sql")
    void shouldNotFindInactivePromoCode() {
        // Arrange
        String code = "INACTIVE10";
        LocalDate currentDate = LocalDate.now();

        // Act
        Optional<PromoCode> foundPromoCode = promoCodeRepository
                .findByPromoCodeAndIsAvailableTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        code, currentDate, currentDate);

        // Assert
        assertFalse(foundPromoCode.isPresent());
    }
} 