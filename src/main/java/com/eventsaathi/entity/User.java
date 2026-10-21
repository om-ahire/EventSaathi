package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Core user entity. A single user can hold multiple roles (Customer + Vendor
 * simultaneously is valid — e.g., a photographer who also books other vendors).
 *
 * DESIGN NOTE – @ManyToMany EAGER for roles:
 *   Spring Security's UserDetailsService needs the full list of GrantedAuthority
 *   objects to build the Authentication object. Fetching roles lazily would cause
 *   a LazyInitializationException outside a transaction. EAGER fetch is acceptable
 *   here because the roles set is tiny (max 3 rows per user) and is always needed.
 *
 * The user_roles join table has NO entity class — @JoinTable handles it.
 * This is appropriate when the join table has no extra columns.
 */
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_city",  columnList = "city_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uq_user_email", columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt-encoded; never store plaintext

    @Column(length = 15)
    private String phone;

    // Nullable – a user might not belong to a specific city (e.g., admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns         = @JoinColumn(name = "user_id",  referencedColumnName = "id"),
            inverseJoinColumns  = @JoinColumn(name = "role_id",  referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Lifecycle hooks ───────────────────────────────────────────────────────

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

    protected User() {}

    public User(String name, String email, String password, String phone) {
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.phone    = phone;
    }

    // ── Role helpers ──────────────────────────────────────────────────────────

    public void addRole(Role role) {
        this.roles.add(role);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
