package com.eventsaathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log for every AI recommendation request.
 *
 * WHY log AI calls?
 *   1. COST CONTROL: Token usage is billable. We need to track costs per user/request.
 *   2. DEBUGGING: If the AI returns bad recommendations, we can replay the exact prompt.
 *   3. COMPLIANCE: Some regulatory contexts require auditability of AI decisions.
 *   4. FALLBACK TRACKING: We can measure how often the fallback triggers and why.
 *
 * The payload columns store raw JSON strings rather than a JSON column type
 * for maximum portability across MySQL versions.
 */
@Entity
@Table(name = "ai_recommendation_logs",
        indexes = @Index(name = "idx_ai_log_user", columnList = "user_id"))
public class AiRecommendationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // nullable – anonymous requests possible
    private User user;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload; // JSON: {budget, city, eventType, guestCount, preferences}

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload; // JSON: AI's vendor recommendation list

    @Column(name = "model_used", length = 100)
    private String modelUsed; // e.g., "gemini-2.0-flash"

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    private boolean success;

    @Column(name = "fallback_used")
    private boolean fallbackUsed; // true if rule-based fallback was triggered

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // if success=false, stores the error

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    protected AiRecommendationLog() {}

    public AiRecommendationLog(User user, String requestPayload) {
        this.user           = user;
        this.requestPayload = requestPayload;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
