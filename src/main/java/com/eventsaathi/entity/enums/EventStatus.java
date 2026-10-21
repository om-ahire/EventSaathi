package com.eventsaathi.entity.enums;

/**
 * Lifecycle states for a customer's event plan.
 *
 * DRAFT     → customer is still planning (default state)
 * ACTIVE    → at least one booking confirmed; event is live
 * COMPLETED → event date has passed
 * CANCELLED → customer cancelled the event
 */
public enum EventStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    CANCELLED
}
