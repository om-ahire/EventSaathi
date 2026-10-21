package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Line items that make up a booking (e.g., "Flowers – 200 units – ₹50 each").
 * Optional detail that vendors can populate when confirming a booking.
 */
@Entity
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 300)
    private String description;

    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    // ── Constructors ──────────────────────────────────────────────────────────

    protected BookingItem() {}

    public BookingItem(Booking booking, String description, int quantity,
                       BigDecimal unitPrice, BigDecimal totalPrice) {
        this.booking     = booking;
        this.description = description;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.totalPrice  = totalPrice;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
