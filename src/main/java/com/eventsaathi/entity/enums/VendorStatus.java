package com.eventsaathi.entity.enums;

/**
 * Lifecycle states for a vendor account.
 * A vendor must be APPROVED by an ADMIN before they appear in customer search results.
 *
 * State machine:
 *   PENDING_APPROVAL → APPROVED  (admin approves)
 *   PENDING_APPROVAL → REJECTED  (admin rejects)
 *   APPROVED         → SUSPENDED (admin suspends)
 *   SUSPENDED        → APPROVED  (admin reinstates)
 */
public enum VendorStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED
}
