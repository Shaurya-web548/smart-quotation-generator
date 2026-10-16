package com.smartquote.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartquote.dto.AuthRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Protected endpoints should reject unauthenticated requests with 401")
    void testProtectedEndpointRejectsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/quotations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should successfully register a new user, login, and access protected /me endpoint")
    void testRegisterLoginAndAccessProtected() throws Exception {
        String uniqueEmail = "owner_" + System.currentTimeMillis() + "@acme.com";

        AuthRequest.Register registerReq = new AuthRequest.Register(
                "Acme Owner",
                uniqueEmail,
                "secret123",
                "Acme Global Enterprises"
        );

        // 1. Register
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(uniqueEmail))
                .andExpect(jsonPath("$.data.company.name").value("Acme Global Enterprises"))
                .andReturn();

        // 2. Login
        AuthRequest.Login loginReq = new AuthRequest.Login(uniqueEmail, "secret123");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("token").asText();

        // 3. Access protected /api/v1/auth/me with Bearer token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value(uniqueEmail));
    }
}
