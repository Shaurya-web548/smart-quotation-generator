package com.smartquote.entity;

public enum QuotationStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    REJECTED,
    EXPIRED;

    public boolean canTransitionTo(QuotationStatus target) {
        if (this == target) return true;
        return switch (this) {
            case DRAFT -> target == SENT || target == ACCEPTED || target == REJECTED || target == EXPIRED;
            case SENT -> target == ACCEPTED || target == REJECTED || target == EXPIRED || target == DRAFT;
            case ACCEPTED, REJECTED, EXPIRED -> false; // Finalized states cannot transition
        };
    }
}
