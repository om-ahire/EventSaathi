package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A review written by a Customer after a booking is COMPLETED.
 *
 * CONSTRAINT: One review per booking (1:1 with Booking).
 * BUSINESS RULE: A review can only be submitted when booking status = COMPLETED.
 *   This is enforced in the service layer (Phase 4+).
 *
 * The vendor rating is re-computed in VendorService after each new review:
 *   vendor.rating = AVG(reviews.rating) WHERE vendor_id = ?
 *   vendor.totalReviews = COUNT(*)
 */
@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uq_review_booking", columnNames = "booking_id"),
        indexes = @Index(name = "idx_review_vendor", columnList = "vendor_id"))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false)
    private Integer rating; // 1–5 stars, validated by @Min(1) @Max(5) in DTO

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected Review() {}

    public Review(Booking booking, User customer, Vendor vendor, Integer rating, String comment) {
        this.booking  = booking;
        this.customer = customer;
        this.vendor   = vendor;
        this.rating   = rating;
        this.comment  = comment;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
