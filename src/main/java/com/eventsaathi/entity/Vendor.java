package com.eventsaathi.entity;

import com.eventsaathi.entity.enums.VendorStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The central entity of the platform. A Vendor is a business registered
 * by a User with ROLE_VENDOR.
 *
 * DESIGN – @OneToOne with User (not @ManyToOne):
 *   Each user account can register at most ONE vendor profile. This models
 *   the real-world constraint that one email = one business on the platform.
 *   We use cascade = PERSIST, MERGE (not ALL) so that deleting a Vendor does
 *   NOT cascade to the User account — the user retains their login.
 *
 * INDEXED COLUMNS (for filtered search performance):
 *   city_id, category_id, base_price, status
 *   These are the fields customers filter on most often.
 */
@Entity
@Table(name = "vendors",
        indexes = {
                @Index(name = "idx_vendor_city",       columnList = "city_id"),
                @Index(name = "idx_vendor_category",   columnList = "category_id"),
                @Index(name = "idx_vendor_base_price", columnList = "base_price"),
                @Index(name = "idx_vendor_status",     columnList = "status")
        },
        uniqueConstraints = @UniqueConstraint(name = "uq_vendor_user", columnNames = "user_id"))
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private VendorCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VendorStatus status = VendorStatus.PENDING_APPROVAL;

    // Computed field – updated by a @Scheduled job or on review save
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(length = 300)
    private String address;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "website_url", length = 300)
    private String websiteUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by_id")
    private Long approvedById; // Admin user ID who approved

    // ── Child collections ─────────────────────────────────────────────────────
    // cascade = ALL + orphanRemoval = true: deleting a vendor cascades to its services and images

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorService> services = new ArrayList<>();

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorImage> images = new ArrayList<>();

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

    protected Vendor() {}

    public Vendor(User user, String businessName, VendorCategory category, City city) {
        this.user         = user;
        this.businessName = businessName;
        this.category     = category;
        this.city         = city;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public VendorCategory getCategory() { return category; }
    public void setCategory(VendorCategory category) { this.category = category; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public VendorStatus getStatus() { return status; }
    public void setStatus(VendorStatus status) { this.status = status; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public List<VendorService> getServices() { return services; }
    public List<VendorImage> getImages() { return images; }
}
