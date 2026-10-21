package com.eventsaathi.entity;

import jakarta.persistence.*;

/**
 * Types of events that customers can plan (Wedding, Corporate, Birthday, etc.).
 * A lookup table so ADMIN can add new event types without code changes.
 */
@Entity
@Table(name = "event_types",
        uniqueConstraints = @UniqueConstraint(name = "uq_event_type_name", columnNames = "name"))
public class EventType {

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

    protected EventType() {}

    public EventType(String name, String description, String icon) {
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
