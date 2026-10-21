package com.eventsaathi.entity;

import com.eventsaathi.entity.enums.RoleName;
import jakarta.persistence.*;

/**
 * Represents a security role. Stored as a separate table so roles can be
 * managed without code changes (ADMIN can create new roles via DB if needed).
 *
 * WHY @Enumerated(STRING)?
 *   @Enumerated(ORDINAL) is fragile — adding a new enum value changes ordinals
 *   for all existing rows. STRING stores "ROLE_ADMIN" which is self-documenting
 *   and safe even if the enum order changes.
 */
@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uq_role_name", columnNames = "name"))
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoleName name;

    // ── Constructors ──────────────────────────────────────────────────────────

    protected Role() {}

    public Role(RoleName name) {
        this.name = name;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public RoleName getName() { return name; }
    public void setName(RoleName name) { this.name = name; }
}
