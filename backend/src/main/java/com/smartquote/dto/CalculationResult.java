package com.smartquote.dto;

import java.math.BigDecimal;
import java.util.List;

public class CalculationResult {
    private BigDecimal subtotal;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal taxableAmount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private List<CalculatedItem> items;

    public static class CalculatedItem {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private Integer sortOrder;

        public CalculatedItem(String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount, Integer sortOrder) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.amount = amount;
            this.sortOrder = sortOrder;
        }

        public String getDescription() { return description; }
        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getAmount() { return amount; }
        public Integer getSortOrder() { return sortOrder; }
    }

    public CalculationResult(BigDecimal subtotal, BigDecimal discountPercentage, BigDecimal discountAmount,
                             BigDecimal taxableAmount, BigDecimal taxPercentage, BigDecimal taxAmount,
                             BigDecimal totalAmount, List<CalculatedItem> items) {
        this.subtotal = subtotal;
        this.discountPercentage = discountPercentage;
        this.discountAmount = discountAmount;
        this.taxableAmount = taxableAmount;
        this.taxPercentage = taxPercentage;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<CalculatedItem> getItems() { return items; }
}
