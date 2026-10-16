package com.smartquote.controller;

import com.smartquote.dto.ApiResponse;
import com.smartquote.dto.CompanyDto;
import com.smartquote.dto.CompanyUpdateRequest;
import com.smartquote.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/settings/company")
@Tag(name = "Company Settings", description = "Tenant company settings, tax rates, currencies, and branding")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    @Operation(summary = "Get current business profile and defaults")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompanySettings() {
        return ResponseEntity.ok(ApiResponse.success(companyService.getCompanySettings()));
    }

    @PutMapping
    @Operation(summary = "Update business profile, tax rate, and quotation defaults")
    public ResponseEntity<ApiResponse<CompanyDto>> updateCompanySettings(@Valid @RequestBody CompanyUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Company settings updated successfully", companyService.updateCompanySettings(request)));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload business logo (PNG, JPEG, SVG, WEBP up to 2MB)")
    public ResponseEntity<ApiResponse<CompanyDto>> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded successfully", companyService.uploadLogo(file)));
    }
}
