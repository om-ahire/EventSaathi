package com.eventsaathi.controller;

import com.eventsaathi.dto.ApiDtos.*;
import com.eventsaathi.security.UserPrincipal;
import com.eventsaathi.service.BookingPlatformService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
    private final BookingPlatformService bookingService;

    public BookingController(BookingPlatformService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public BookingResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                  @Valid @RequestBody BookingRequest request) {
        return bookingService.create(principal, request);
    }

    @GetMapping("/bookings/mine")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return bookingService.customerBookings(principal);
    }

    @GetMapping("/vendor/bookings")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public List<BookingResponse> vendorBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return bookingService.vendorBookings(principal);
    }

    @PatchMapping("/vendor/bookings/{id}/accept")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public BookingResponse accept(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                  @RequestBody(required = false) BookingDecisionRequest request) {
        return bookingService.decide(principal, id, true, request == null ? null : request.note());
    }

    @PatchMapping("/vendor/bookings/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public BookingResponse reject(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                  @RequestBody(required = false) BookingDecisionRequest request) {
        return bookingService.decide(principal, id, false, request == null ? null : request.note());
    }

    @PatchMapping("/vendor/bookings/{id}/complete")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public BookingResponse complete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return bookingService.complete(principal, id);
    }

    @PatchMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_VENDOR')")
    public BookingResponse cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                  @RequestBody(required = false) BookingDecisionRequest request) {
        return bookingService.cancel(principal, id, request == null ? null : request.note());
    }

    @PostMapping("/bookings/{id}/review")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ReviewResponse review(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                 @Valid @RequestBody ReviewRequest request) {
        return bookingService.review(principal, id, request);
    }

    @PostMapping("/bookings/{id}/payment")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public PaymentResponse payment(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                  @Valid @RequestBody PaymentRequest request) {
        return bookingService.simulatePayment(principal, id, request);
    }
}
