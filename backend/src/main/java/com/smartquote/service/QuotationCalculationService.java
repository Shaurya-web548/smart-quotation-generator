package com.smartquote.service;

import com.smartquote.dto.CalculationResult;
import com.smartquote.dto.QuotationItemDto;
import com.smartquote.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * QuotationCalculationService
 *
 * Implements strict monetary calculations using BigDecimal and explicit RoundingMode.HALF_UP.
 *
 * Formula:
 *   1. item.amount = round(item.quantity * item.unitPrice, 2)
 *   2. subtotal = sum(item.amount)
 *   3. discountAmount = round(subtotal * discountPercentage / 100, 2)
 *   4. taxableAmount = subtotal - discountAmount
 *   5. taxAmount = round(taxableAmount * taxPercentage / 100, 2)
 *   6. totalAmount = taxableAmount + taxAmount
 */
@Service
public class QuotationCalculationService {

    private static final int MONETARY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    public CalculationResult calculate(
            List<QuotationItemDto> items,
            BigDecimal discountPercentage,
            BigDecimal taxPercentage
    ) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one line item is required");
        }

        if (discountPercentage == null) {
            discountPercentage = BigDecimal.ZERO.setScale(MONETARY_SCALE, ROUNDING_MODE);
        } else {
            if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || discountPercentage.compareTo(HUNDRED) > 0) {
                throw new BadRequestException("Discount percentage must be between 0 and 100");
            }
            discountPercentage = discountPercentage.setScale(MONETARY_SCALE, ROUNDING_MODE);
        }

        if (taxPercentage == null) {
            taxPercentage = BigDecimal.ZERO.setScale(MONETARY_SCALE, ROUNDING_MODE);
        } else {
            if (taxPercentage.compareTo(BigDecimal.ZERO) < 0 || taxPercentage.compareTo(HUNDRED) > 0) {
                throw new BadRequestException("Tax percentage must be between 0 and 100");
            }
            taxPercentage = taxPercentage.setScale(MONETARY_SCALE, ROUNDING_MODE);
        }

        BigDecimal subtotal = BigDecimal.ZERO.setScale(MONETARY_SCALE, ROUNDING_MODE);
        List<CalculationResult.CalculatedItem> calculatedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            QuotationItemDto it = items.get(i);
            if (it.getDescription() == null || it.getDescription().trim().isEmpty()) {
                throw new BadRequestException("Item #" + (i + 1) + " description cannot be empty");
            }
            if (it.getQuantity() == null || it.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Item '" + it.getDescription() + "' quantity must be greater than zero");
            }
            if (it.getUnitPrice() == null || it.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Item '" + it.getDescription() + "' unit price cannot be negative");
            }

            BigDecimal itemAmount = it.getQuantity().multiply(it.getUnitPrice()).setScale(MONETARY_SCALE, ROUNDING_MODE);
            subtotal = subtotal.add(itemAmount);

            calculatedItems.add(new CalculationResult.CalculatedItem(
                    it.getDescription().trim(),
                    it.getQuantity(),
                    it.getUnitPrice().setScale(MONETARY_SCALE, ROUNDING_MODE),
                    itemAmount,
                    it.getSortOrder() != null ? it.getSortOrder() : i
            ));
        }

        // discountAmount = subtotal * discountPercentage / 100
        BigDecimal discountAmount = subtotal.multiply(discountPercentage)
                .divide(HUNDRED, MONETARY_SCALE, ROUNDING_MODE);

        // taxableAmount = subtotal - discountAmount
        BigDecimal taxableAmount = subtotal.subtract(discountAmount).setScale(MONETARY_SCALE, ROUNDING_MODE);

        // taxAmount = taxableAmount * taxPercentage / 100
        BigDecimal taxAmount = taxableAmount.multiply(taxPercentage)
                .divide(HUNDRED, MONETARY_SCALE, ROUNDING_MODE);

        // totalAmount = taxableAmount + taxAmount
        BigDecimal totalAmount = taxableAmount.add(taxAmount).setScale(MONETARY_SCALE, ROUNDING_MODE);

        return new CalculationResult(
                subtotal,
                discountPercentage,
                discountAmount,
                taxableAmount,
                taxPercentage,
                taxAmount,
                totalAmount,
                calculatedItems
        );
    }
}

