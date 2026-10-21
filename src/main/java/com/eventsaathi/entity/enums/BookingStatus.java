package com.eventsaathi.entity.enums;

/**
 * Booking lifecycle states.
 *
 * Flow:
 *   PENDING    → CONFIRMED  (vendor accepts)
 *   PENDING    → REJECTED   (vendor rejects)
 *   CONFIRMED  → COMPLETED  (event date has passed, service delivered)
 *   CONFIRMED  → CANCELLED  (customer or vendor cancels)
 *   PENDING    → CANCELLED  (customer cancels before vendor responds)
 *
 * Terminal states: REJECTED, COMPLETED, CANCELLED
 * A vendor slot is "occupied" only for PENDING and CONFIRMED bookings.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    COMPLETED,
    CANCELLED
}
