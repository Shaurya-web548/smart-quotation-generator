package com.smartquote.controller;

import com.smartquote.dto.*;
import com.smartquote.entity.QuotationStatus;
import com.smartquote.service.PdfService;
import com.smartquote.service.QuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/quotations")
@Tag(name = "Quotations", description = "Quotation creation, calculation, lifecycle, and PDF export")
public class QuotationController {

    private final QuotationService quotationService;
    private final PdfService pdfService;

    public QuotationController(QuotationService quotationService, PdfService pdfService) {
        this.quotationService = quotationService;
        this.pdfService = pdfService;
    }

    @GetMapping
    @Operation(summary = "Get paginated quotation list with optional search and filters")
    public ResponseEntity<ApiResponse<PageResponse<QuotationResponseDto>>> getQuotations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponse<QuotationResponseDto> response = quotationService.getQuotations(
                search, status, startDate, endDate, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new quotation with validated backend calculations")
    public ResponseEntity<ApiResponse<QuotationResponseDto>> createQuotation(
            @Valid @RequestBody QuotationCreateRequest request
    ) {
        QuotationResponseDto response = quotationService.createQuotation(request);
        return ResponseEntity.ok(ApiResponse.success("Quotation created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed quotation by ID (tenant isolated)")
    public ResponseEntity<ApiResponse<QuotationResponseDto>> getQuotationById(@PathVariable Long id) {
        QuotationResponseDto response = quotationService.getQuotationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing draft or sent quotation")
    public ResponseEntity<ApiResponse<QuotationResponseDto>> updateQuotation(
            @PathVariable Long id,
            @Valid @RequestBody QuotationCreateRequest request
    ) {
        QuotationResponseDto response = quotationService.updateQuotation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Quotation updated successfully", response));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate a quotation into a new DRAFT")
    public ResponseEntity<ApiResponse<QuotationResponseDto>> duplicateQuotation(@PathVariable Long id) {
        QuotationResponseDto response = quotationService.duplicateQuotation(id);
        return ResponseEntity.ok(ApiResponse.success("Quotation duplicated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update quotation status with transition validation")
    public ResponseEntity<ApiResponse<QuotationResponseDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody QuotationStatusUpdateRequest request
    ) {
        QuotationResponseDto response = quotationService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Quotation status updated", response));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download authorized PDF quotation document")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) throws IOException {
        QuotationResponseDto quotation = quotationService.getQuotationById(id);
        byte[] pdfBytes = pdfService.generateQuotationPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", quotation.getQuotationNumber() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
