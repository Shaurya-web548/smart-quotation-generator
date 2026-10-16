package com.smartquote.dto;

import com.smartquote.entity.Company;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CompanyDto {
    private Long id;
    private String name;
    private String logoUrl;
    private String address;
    private String phone;
    private String email;
    private String taxId;
    private String currency;
    private String currencySymbol;
    private BigDecimal defaultTaxRate;
    private Integer defaultValidityDays;
    private String defaultTerms;

    public CompanyDto() {}

    public static CompanyDto fromEntity(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setAddress(company.getAddress());
        dto.setPhone(company.getPhone());
        dto.setEmail(company.getEmail());
        dto.setTaxId(company.getTaxId());
        dto.setCurrency(company.getCurrency());
        dto.setCurrencySymbol(company.getCurrencySymbol());
        dto.setDefaultTaxRate(company.getDefaultTaxRate());
        dto.setDefaultValidityDays(company.getDefaultValidityDays());
        dto.setDefaultTerms(company.getDefaultTerms());
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
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
