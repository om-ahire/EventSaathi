package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Portfolio/gallery images for a vendor.
 * The 'is_primary' flag identifies the thumbnail shown in listing cards.
 *
 * NOTE: We store image URLs (pointing to a CDN or object storage) rather than
 * binary blobs in the DB. Storing BLOBs in MySQL is an anti-pattern — it bloats
 * the DB, slows backups, and prevents CDN delivery.
 */
@Entity
@Table(name = "vendor_images",
        indexes = @Index(name = "idx_vi_vendor_primary", columnList = "vendor_id, is_primary"))
public class VendorImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary")
    private boolean primary = false;

    @Column(length = 200)
    private String caption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected VendorImage() {}

    public VendorImage(Vendor vendor, String imageUrl, boolean primary, String caption) {
        this.vendor   = vendor;
        this.imageUrl = imageUrl;
        this.primary  = primary;
        this.caption  = caption;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
