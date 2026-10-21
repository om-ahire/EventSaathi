package com.eventsaathi.entity.enums;

/**
 * Payment lifecycle – mock implementation for Phase 1.
 * In a real system this would integrate with a payment gateway (Razorpay, Stripe).
 *
 * PENDING   → payment record created, awaiting confirmation
 * COMPLETED → payment captured successfully
 * FAILED    → gateway declined
 * REFUNDED  → amount returned to customer
 */
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
