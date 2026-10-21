package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Tracks dates when a vendor is unavailable (e.g., fully booked or personal leave).
 *
 * DESIGN NOTE – "closed dates" model vs "open slots" model:
 *   This table uses the closed-dates model: by default every date is AVAILABLE
 *   and records here mark exceptions (blocked/unavailable dates).
 *   The alternative (open-slots model) requires seeding every available day,
 *   which is impractical for a year-round vendor.
 *
 * The unique constraint prevents duplicate entries for the same vendor+date.
 * Double-booking prevention is enforced in the Booking table (Phase 4).
 */
@Entity
@Table(name = "vendor_availability",
        uniqueConstraints = @UniqueConstraint(
                name  = "uq_vendor_date",
                columnNames = {"vendor_id", "available_date"}),
        indexes = @Index(name = "idx_va_vendor_date", columnList = "vendor_id, available_date"))
public class VendorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "is_available")
    private boolean available = true;

    @Column(length = 200)
    private String notes; // e.g., "Booked for another event"

    // ── Constructors ──────────────────────────────────────────────────────────

    protected VendorAvailability() {}

    public VendorAvailability(Vendor vendor, LocalDate date, boolean available, String notes) {
        this.vendor        = vendor;
        this.availableDate = date;
        this.available     = available;
        this.notes         = notes;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
