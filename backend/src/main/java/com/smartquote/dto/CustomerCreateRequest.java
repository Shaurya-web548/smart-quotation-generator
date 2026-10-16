package com.smartquote.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CustomerCreateRequest {
    @NotBlank(message = "Customer name is required")
    private String name;

    @Email(message = "Customer email must be valid")
    private String email;

    private String phone;
    private String address;
    private String taxId;

    public CustomerCreateRequest() {}

    public CustomerCreateRequest(String name, String email, String phone, String address, String taxId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.taxId = taxId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
}
