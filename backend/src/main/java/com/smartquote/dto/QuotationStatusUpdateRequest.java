package com.smartquote.dto;

import com.smartquote.entity.QuotationStatus;
import jakarta.validation.constraints.NotNull;

public class QuotationStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private QuotationStatus status;

    public QuotationStatusUpdateRequest() {}
    public QuotationStatusUpdateRequest(QuotationStatus status) {
        this.status = status;
    }

    public QuotationStatus getStatus() { return status; }
    public void setStatus(QuotationStatus status) { this.status = status; }
}
