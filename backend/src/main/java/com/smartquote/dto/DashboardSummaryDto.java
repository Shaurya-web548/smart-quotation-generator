package com.smartquote.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDto {
    private long totalQuotations;
    private long draftQuotations;
    private long sentQuotations;
    private long acceptedQuotations;
    private long rejectedQuotations;
    private long expiredQuotations;
    private BigDecimal totalValue;
    private BigDecimal acceptedValue;
    private List<QuotationResponseDto> recentQuotations;

    public DashboardSummaryDto() {}

    public long getTotalQuotations() { return totalQuotations; }
    public void setTotalQuotations(long totalQuotations) { this.totalQuotations = totalQuotations; }

    public long getDraftQuotations() { return draftQuotations; }
    public void setDraftQuotations(long draftQuotations) { this.draftQuotations = draftQuotations; }

    public long getSentQuotations() { return sentQuotations; }
    public void setSentQuotations(long sentQuotations) { this.sentQuotations = sentQuotations; }

    public long getAcceptedQuotations() { return acceptedQuotations; }
    public void setAcceptedQuotations(long acceptedQuotations) { this.acceptedQuotations = acceptedQuotations; }

    public long getRejectedQuotations() { return rejectedQuotations; }
    public void setRejectedQuotations(long rejectedQuotations) { this.rejectedQuotations = rejectedQuotations; }

    public long getExpiredQuotations() { return expiredQuotations; }
    public void setExpiredQuotations(long expiredQuotations) { this.expiredQuotations = expiredQuotations; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getAcceptedValue() { return acceptedValue; }
    public void setAcceptedValue(BigDecimal acceptedValue) { this.acceptedValue = acceptedValue; }

    public List<QuotationResponseDto> getRecentQuotations() { return recentQuotations; }
    public void setRecentQuotations(List<QuotationResponseDto> recentQuotations) { this.recentQuotations = recentQuotations; }
}

