package com.smartquote.dto;

import com.smartquote.entity.Quotation;
import com.smartquote.entity.QuotationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuotationResponseDto {
    private Long id;
    private String quotationNumber;
    private Long companyId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private QuotationStatus status;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private BigDecimal subtotal;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal taxableAmount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private String terms;
    private List<QuotationItemDto> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QuotationResponseDto() {}

    public static QuotationResponseDto fromEntity(Quotation quotation) {
        QuotationResponseDto dto = new QuotationResponseDto();
        dto.setId(quotation.getId());
        dto.setQuotationNumber(quotation.getQuotationNumber());
        dto.setCompanyId(quotation.getCompany().getId());
        if (quotation.getCustomer() != null) {
            dto.setCustomerId(quotation.getCustomer().getId());
        }
        dto.setCustomerName(quotation.getCustomerName());
        dto.setCustomerEmail(quotation.getCustomerEmail());
        dto.setCustomerPhone(quotation.getCustomerPhone());
        dto.setCustomerAddress(quotation.getCustomerAddress());
        dto.setStatus(quotation.getStatus());
        dto.setIssueDate(quotation.getIssueDate());
        dto.setExpiryDate(quotation.getExpiryDate());
        dto.setSubtotal(quotation.getSubtotal());
        dto.setDiscountPercentage(quotation.getDiscountPercentage());
        dto.setDiscountAmount(quotation.getDiscountAmount());
        dto.setTaxableAmount(quotation.getTaxableAmount());
        dto.setTaxPercentage(quotation.getTaxPercentage());
        dto.setTaxAmount(quotation.getTaxAmount());
        dto.setTotalAmount(quotation.getTotalAmount());
        dto.setNotes(quotation.getNotes());
        dto.setTerms(quotation.getTerms());
        dto.setCreatedAt(quotation.getCreatedAt());
        dto.setUpdatedAt(quotation.getUpdatedAt());

        if (quotation.getItems() != null) {
            dto.setItems(quotation.getItems().stream()
                    .map(QuotationItemDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public QuotationStatus getStatus() { return status; }
    public void setStatus(QuotationStatus status) { this.status = status; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }
    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
    public List<QuotationItemDto> getItems() { return items; }
    public void setItems(List<QuotationItemDto> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
