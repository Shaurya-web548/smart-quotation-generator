package com.smartquote.controller;

import com.smartquote.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application health and status check")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check backend service health")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkHealth() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "UP",
                "app", "Smart Quotation Generator",
                "version", "1.0.0"
        )));
    }
}
