package com.eventsaathi.dto;

import com.eventsaathi.entity.enums.BookingStatus;
import com.eventsaathi.entity.enums.VendorStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @Size(max = 15) String phone) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record AuthResponse(
            String token, String tokenType, long expiresInSeconds, UserResponse user) {}

    public record UserResponse(Long id, String name, String email, String phone, Set<String> roles) {}

    public record VendorProfileRequest(
            @NotBlank @Size(max = 200) String businessName,
            @NotNull Long categoryId,
            @NotNull Long cityId,
            @Size(max = 2000) String description,
            @PositiveOrZero BigDecimal basePrice,
            @Size(max = 300) String address,
            @Email @Size(max = 150) String contactEmail,
            @Size(max = 15) String contactPhone,
            @Size(max = 300) String websiteUrl) {}

    public record VendorSummary(
            Long id, String businessName, String description, String category, Long categoryId,
            String city, Long cityId, BigDecimal basePrice, BigDecimal rating, Integer totalReviews,
            VendorStatus status, String address, String contactEmail, String contactPhone,
            String websiteUrl, String imageUrl, List<VendorServiceResponse> services) {}

    public record VendorServiceRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 2000) String description,
            @NotNull @PositiveOrZero BigDecimal price,
            @Positive Integer maxGuests,
            @Positive Integer durationHours) {}

    public record VendorServiceResponse(
            Long id, String name, String description, BigDecimal price,
            Integer maxGuests, Integer durationHours) {}

    public record EventRequest(
            @NotBlank @Size(max = 200) String name,
            @NotNull @FutureOrPresent LocalDate eventDate,
            Long eventTypeId,
            Long cityId,
            @Positive Integer guestCount,
            @PositiveOrZero BigDecimal totalBudget,
            @Size(max = 4000) String description) {}

    public record EventResponse(
            Long id, String name, LocalDate eventDate, String eventType, Long eventTypeId,
            String city, Long cityId, Integer guestCount, BigDecimal totalBudget,
            String description, String status) {}

    public record BookingRequest(
            @NotNull Long eventId,
            @NotNull Long vendorId,
            Long vendorServiceId,
            @Size(max = 2000) String notes) {}

    public record BookingDecisionRequest(@Size(max = 2000) String note) {}

    public record BookingResponse(
            Long id, Long eventId, String eventName, Long vendorId, String vendorName,
            Long customerId, String customerName, LocalDate bookingDate, BookingStatus status,
            BigDecimal totalAmount, String notes, String vendorNote, LocalDateTime createdAt) {}

    public record AvailabilityRequest(@NotNull LocalDate date, boolean available, @Size(max = 200) String notes) {}

    public record ReviewRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 2000) String comment) {}

    public record ReviewResponse(Long id, Long bookingId, Long vendorId, String customerName,
                                 Integer rating, String comment, LocalDateTime createdAt) {}

    public record RecommendationRequest(
            @PositiveOrZero BigDecimal budget,
            Long cityId,
            Long categoryId,
            @Positive Integer guestCount,
            @Size(max = 500) String preferences) {}

    public record RecommendedVendor(VendorSummary vendor, String reason) {}

    public record RecommendationResponse(List<RecommendedVendor> vendors, boolean aiAssisted) {}

    public record LookupResponse(Long id, String name, String description, String icon) {}

    public record NotificationResponse(
            Long id, String title, String message, String type, boolean read, LocalDateTime createdAt) {}

    public record VendorStatusRequest(@NotNull VendorStatus status) {}

    public record PaymentRequest(@NotBlank @Size(max = 30) String paymentMethod) {}

    public record PaymentResponse(Long id, Long bookingId, BigDecimal amount, String status,
                                  String paymentMethod, LocalDateTime paidAt) {}

    public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {}
}
