package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * In-app notifications pushed to users on key events:
 *   - Customer: "Your booking was confirmed by Royal Palace"
 *   - Vendor:   "New booking request from Priya for March 15"
 *   - Admin:    "New vendor 'Ananya's Kitchen' is awaiting approval"
 *
 * WHY not email/SMS?
 *   External notification channels add infra dependencies. In-app notifications
 *   demonstrate the pattern; Phase 7 can add email via Spring Mail.
 */
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notif_user", columnList = "user_id"),
                @Index(name = "idx_notif_read", columnList = "user_id, is_read")
        })
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 50)
    private String type; // e.g., "BOOKING_CONFIRMED", "NEW_BOOKING", "VENDOR_APPROVED"

    @Column(name = "is_read")
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected Notification() {}

    public Notification(User user, String title, String message, String type) {
        this.user    = user;
        this.title   = title;
        this.message = message;
        this.type    = type;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
