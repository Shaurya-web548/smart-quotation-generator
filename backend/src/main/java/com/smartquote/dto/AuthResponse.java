package com.smartquote.dto;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserDto user;
    private CompanyDto company;

    public AuthResponse() {}

    public AuthResponse(String token, UserDto user, CompanyDto company) {
        this.token = token;
        this.user = user;
        this.company = company;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

    public CompanyDto getCompany() { return company; }
    public void setCompany(CompanyDto company) { this.company = company; }
}
