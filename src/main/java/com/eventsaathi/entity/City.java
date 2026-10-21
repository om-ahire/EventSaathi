package com.eventsaathi.entity;

import jakarta.persistence.*;

/**
 * Represents an Indian city. Vendors and users are associated to a city.
 *
 * WHY a separate City table?
 *   Normalization (3NF) — storing "city" as a plain varchar in users/vendors
 *   would allow inconsistent spellings ("Mumbai" vs "mumbai" vs "Bombay").
 *   A FK to a cities table enforces a controlled vocabulary and lets us
 *   filter/search on city_id with an index instead of a full-text scan.
 */
@Entity
@Table(name = "cities",
        uniqueConstraints = @UniqueConstraint(name = "uq_city_name", columnNames = "name"))
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 50)
    private String country = "India";

    // ── Constructors ──────────────────────────────────────────────────────────

    protected City() {} // JPA requires a no-arg constructor

    public City(String name, String state, String country) {
        this.name = name;
        this.state = state;
        this.country = country;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
