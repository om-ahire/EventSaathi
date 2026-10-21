package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A service package offered by a vendor (e.g., "Basic Wedding Photography – ₹30,000").
 * A vendor can have multiple packages at different price points.
 *
 * WHY a separate table and not a JSON column?
 *   JSON columns are not indexable and violate 1NF. A separate table lets us
 *   query "find all packages under ₹50,000" with a proper B-tree index.
 *
 * The 'price' column is INDEXED so the customer can filter by budget efficiently.
 */
@Entity
@Table(name = "vendor_services",
        indexes = @Index(name = "idx_vs_price", columnList = "price"))
public class VendorService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "max_guests")
    private Integer maxGuests;

    @Column(name = "duration_hours")
    private Integer durationHours;

    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected VendorService() {}

    public VendorService(Vendor vendor, String name, String description,
                         BigDecimal price, Integer maxGuests, Integer durationHours) {
        this.vendor        = vendor;
        this.name          = name;
        this.description   = description;
        this.price         = price;
        this.maxGuests     = maxGuests;
        this.durationHours = durationHours;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getMaxGuests() { return maxGuests; }
    public void setMaxGuests(Integer maxGuests) { this.maxGuests = maxGuests; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
