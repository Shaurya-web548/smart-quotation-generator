package com.smartquote.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CompanyUpdateRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    private String address;
    private String phone;
    private String email;
    private String taxId;
    private String logoUrl;

    @NotBlank(message = "Currency code is required")
    private String currency;

    @NotBlank(message = "Currency symbol is required")
    private String currencySymbol;

    @NotNull(message = "Default tax rate is required")
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    private BigDecimal defaultTaxRate;

    @NotNull(message = "Default validity days is required")
    private Integer defaultValidityDays;

    private String defaultTerms;

    public CompanyUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public BigDecimal getDefaultTaxRate() { return defaultTaxRate; }
    public void setDefaultTaxRate(BigDecimal defaultTaxRate) { this.defaultTaxRate = defaultTaxRate; }
    public Integer getDefaultValidityDays() { return defaultValidityDays; }
    public void setDefaultValidityDays(Integer defaultValidityDays) { this.defaultValidityDays = defaultValidityDays; }
    public String getDefaultTerms() { return defaultTerms; }
    public void setDefaultTerms(String defaultTerms) { this.defaultTerms = defaultTerms; }
}

