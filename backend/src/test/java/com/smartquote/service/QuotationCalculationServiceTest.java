package com.smartquote.service;

import com.smartquote.dto.CalculationResult;
import com.smartquote.dto.QuotationItemDto;
import com.smartquote.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuotationCalculationServiceTest {

    private QuotationCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new QuotationCalculationService();
    }

    @Test
    @DisplayName("Should correctly calculate totals with multiple items, discount, and GST matching legacy sample")
    void testStandardCalculation() {
        // Legacy sample:
        // Widget|2|150.00 = 300.00
        // Gadget|1|99.50  = 99.50
        // Subtotal = 399.50
        // Discount 10% = 39.95
        // Taxable = 359.55
        // GST 18% = 64.719 -> 64.72
        // Total = 424.27
        List<QuotationItemDto> items = List.of(
                new QuotationItemDto("Widget", new BigDecimal("2"), new BigDecimal("150.00")),
                new QuotationItemDto("Gadget", new BigDecimal("1"), new BigDecimal("99.50"))
        );

        CalculationResult result = calculationService.calculate(
                items,
                new BigDecimal("10.00"),
                new BigDecimal("18.00")
        );

        assertEquals(new BigDecimal("399.50"), result.getSubtotal());
        assertEquals(new BigDecimal("39.95"), result.getDiscountAmount());
        assertEquals(new BigDecimal("359.55"), result.getTaxableAmount());
        assertEquals(new BigDecimal("64.72"), result.getTaxAmount());
        assertEquals(new BigDecimal("424.27"), result.getTotalAmount());
        assertEquals(2, result.getItems().size());
    }

    @Test
    @DisplayName("Should correctly calculate totals with zero discount and zero tax")
    void testZeroDiscountAndZeroTax() {
        List<QuotationItemDto> items = List.of(
                new QuotationItemDto("Service Consulting", new BigDecimal("5"), new BigDecimal("1200.00"))
        );

        CalculationResult result = calculationService.calculate(
                items,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        assertEquals(new BigDecimal("6000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("6000.00"), result.getTaxableAmount());
        assertEquals(new BigDecimal("0.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("6000.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Should handle fractional quantities and explicit half-up rounding")
    void testRoundingPolicy() {
        // 3.33 qty * 10.55 price = 35.1315 -> 35.13
        // Discount 5.5% on 35.13 = 1.93215 -> 1.93
        // Taxable = 33.20
        // Tax 12% on 33.20 = 3.984 -> 3.98
        // Total = 37.18
        List<QuotationItemDto> items = List.of(
                new QuotationItemDto("Bulk Material", new BigDecimal("3.33"), new BigDecimal("10.55"))
        );

        CalculationResult result = calculationService.calculate(
                items,
                new BigDecimal("5.50"),
                new BigDecimal("12.00")
        );

        assertEquals(new BigDecimal("35.13"), result.getSubtotal());
        assertEquals(new BigDecimal("1.93"), result.getDiscountAmount());
        assertEquals(new BigDecimal("33.20"), result.getTaxableAmount());
        assertEquals(new BigDecimal("3.98"), result.getTaxAmount());
        assertEquals(new BigDecimal("37.18"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Should reject empty items list")
    void testEmptyItemsRejected() {
        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(Collections.emptyList(), BigDecimal.ZERO, BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Should reject negative or zero quantities")
    void testInvalidQuantityRejected() {
        List<QuotationItemDto> items = List.of(
                new QuotationItemDto("Invalid Item", new BigDecimal("0"), new BigDecimal("100.00"))
        );
        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(items, BigDecimal.ZERO, BigDecimal.ZERO)
        );

        List<QuotationItemDto> negativeQty = List.of(
                new QuotationItemDto("Invalid Item", new BigDecimal("-2"), new BigDecimal("100.00"))
        );
        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(negativeQty, BigDecimal.ZERO, BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Should reject negative unit price")
    void testNegativeUnitPriceRejected() {
        List<QuotationItemDto> items = List.of(
                new QuotationItemDto("Invalid Item", new BigDecimal("1"), new BigDecimal("-50.00"))
        );
        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(items, BigDecimal.ZERO, BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Should reject invalid discount percentages (< 0 or > 100)")
    void testInvalidDiscountPercentage() {
        List<QuotationItemDto> items = List.of(
                new QuotationItemDto("Item", new BigDecimal("1"), new BigDecimal("100.00"))
        );

        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(items, new BigDecimal("-5.00"), BigDecimal.ZERO)
        );

        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(items, new BigDecimal("105.00"), BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Should reject invalid tax percentages (< 0 or > 100)")
    void testInvalidTaxPercentage() {
        List<QuotationItemDto> items = List.of(
                new QuotationItemDto("Item", new BigDecimal("1"), new BigDecimal("100.00"))
        );

        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(items, BigDecimal.ZERO, new BigDecimal("-1.00"))
        );

        assertThrows(BadRequestException.class, () ->
                calculationService.calculate(items, BigDecimal.ZERO, new BigDecimal("150.00"))
        );
    }
}

