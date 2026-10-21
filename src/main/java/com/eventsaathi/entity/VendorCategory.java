package com.eventsaathi.entity;

import jakarta.persistence.*;

/**
 * The 7 vendor categories defined in the product spec:
 *   Venue, Catering, Photography, Decoration, Music/DJ, Makeup, Transport
 *
 * Stored as a controlled lookup table so categories can be added by ADMIN
 * without code deployment. The 'icon' field stores an emoji or CSS icon class
 * used by the frontend.
 */
@Entity
@Table(name = "vendor_categories",
        uniqueConstraints = @UniqueConstraint(name = "uq_category_name", columnNames = "name"))
public class VendorCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String icon;

    // ── Constructors ──────────────────────────────────────────────────────────

    protected VendorCategory() {}

    public VendorCategory(String name, String description, String icon) {
        this.name        = name;
        this.description = description;
        this.icon        = icon;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
