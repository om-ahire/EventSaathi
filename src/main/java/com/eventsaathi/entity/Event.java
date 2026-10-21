package com.eventsaathi.entity;

import com.eventsaathi.entity.enums.EventStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An event planned by a CUSTOMER (e.g., "Priya & Rahul Wedding – March 15, 2025").
 * An event is a container for multiple Bookings (one per vendor).
 *
 * WHY separate Event and Booking?
 *   Separation of concerns and normalization. An event has metadata (name, date,
 *   guest count, budget) while bookings are transactional records with a vendor.
 *   One event can have many bookings — venue + catering + photography, etc.
 */
@Entity
@Table(name = "events",
        indexes = {
                @Index(name = "idx_event_user",   columnList = "user_id"),
                @Index(name = "idx_event_date",   columnList = "event_date"),
                @Index(name = "idx_event_status", columnList = "status")
        })
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id")
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "total_budget", precision = 14, scale = 2)
    private BigDecimal totalBudget;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

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

    protected Event() {}

    public Event(User user, String name, LocalDate eventDate) {
        this.user      = user;
        this.name      = name;
        this.eventDate = eventDate;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<Booking> getBookings() { return bookings; }
}
