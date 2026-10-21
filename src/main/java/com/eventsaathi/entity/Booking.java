package com.eventsaathi.entity;

import com.eventsaathi.entity.enums.BookingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A booking represents a transactional agreement between a Customer and a Vendor
 * for a specific date.
 *
 * DOUBLE-BOOKING PREVENTION:
 *   The service layer checks: does this vendor already have an ACTIVE booking
 *   (PENDING or CONFIRMED) on the requested date? If yes → throw BusinessException.
 *   We don't use a DB unique constraint because REJECTED/CANCELLED bookings should
 *   be allowed to have the same vendor+date (they no longer occupy the slot).
 *   MySQL doesn't support partial/filtered unique indexes.
 *
 * OWNERSHIP RULE (Phase 4):
 *   A customer can only view/cancel their OWN bookings (checked in service layer).
 *   A vendor can only accept/reject bookings for THEIR OWN vendor profile.
 */
@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_booking_event",    columnList = "event_id"),
                @Index(name = "idx_booking_vendor",   columnList = "vendor_id"),
                @Index(name = "idx_booking_customer", columnList = "customer_id"),
                @Index(name = "idx_booking_date",     columnList = "booking_date"),
                @Index(name = "idx_booking_status",   columnList = "status")
        })
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_service_id") // nullable – customer may book without a specific package
    private VendorService vendorService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String notes; // Customer notes for the vendor

    @Column(name = "vendor_note", columnDefinition = "TEXT")
    private String vendorNote; // Vendor's acceptance/rejection message

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Child entities ────────────────────────────────────────────────────────

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingItem> items = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected Booking() {}

    public Booking(Event event, Vendor vendor, User customer, LocalDate bookingDate) {
        this.event       = event;
        this.vendor      = vendor;
        this.customer    = customer;
        this.bookingDate = bookingDate;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public VendorService getVendorService() { return vendorService; }
    public void setVendorService(VendorService vendorService) { this.vendorService = vendorService; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getVendorNote() { return vendorNote; }
    public void setVendorNote(String vendorNote) { this.vendorNote = vendorNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Payment getPayment() { return payment; }
    public Review getReview() { return review; }
    public List<BookingItem> getItems() { return items; }
}
