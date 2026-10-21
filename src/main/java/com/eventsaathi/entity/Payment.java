package com.eventsaathi.entity;

import com.eventsaathi.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mock payment record. In production this would store Razorpay/Stripe
 * order IDs, webhook responses, and a signed payload for idempotency.
 *
 * WHY store payment separately from Booking?
 *   SRP (Single Responsibility Principle) — a booking tracks the vendor
 *   agreement while a payment tracks the financial transaction.
 *   They have different lifecycles: a booking can be confirmed but payment
 *   might fail; the booking still needs to exist with its own audit trail.
 *
 * The @OneToOne FK is on the payments table (not bookings), which is the
 * conventional choice: the "dependent" side holds the FK.
 */
@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uq_payment_booking", columnNames = "booking_id"))
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod; // e.g., "CARD", "UPI", "NET_BANKING" (mock)

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId; // Gateway reference

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected Payment() {}

    public Payment(Booking booking, BigDecimal amount, String paymentMethod) {
        this.booking       = booking;
        this.amount        = amount;
        this.paymentMethod = paymentMethod;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
