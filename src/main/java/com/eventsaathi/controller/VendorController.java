package com.eventsaathi.controller;

import com.eventsaathi.dto.ApiDtos.*;
import com.eventsaathi.entity.enums.VendorStatus;
import com.eventsaathi.security.UserPrincipal;
import com.eventsaathi.service.VendorPlatformService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VendorController {
    private final VendorPlatformService vendorService;

    public VendorController(VendorPlatformService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping("/vendors")
    public Page<VendorSummary> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "rating") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return vendorService.search(categoryId, cityId, maxPrice, q, page, size, sort, direction);
    }

    @GetMapping("/vendors/{id}")
    public VendorSummary getVendor(@PathVariable Long id) {
        return vendorService.getPublicVendor(id);
    }

    @PostMapping("/vendors/me")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_VENDOR')")
    public VendorSummary createProfile(@AuthenticationPrincipal UserPrincipal principal,
                                       @Valid @RequestBody VendorProfileRequest request) {
        return vendorService.createProfile(principal, request);
    }

    @PutMapping("/vendors/me")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_VENDOR')")
    public VendorSummary updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                       @Valid @RequestBody VendorProfileRequest request) {
        return vendorService.updateProfile(principal, request);
    }

    @GetMapping("/vendors/me")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_VENDOR')")
    public VendorSummary myProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return vendorService.myProfile(principal);
    }

    @PostMapping("/vendors/me/services")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public VendorServiceResponse addService(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody VendorServiceRequest request) {
        return vendorService.addService(principal, request);
    }

    @DeleteMapping("/vendors/me/services/{id}")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public void deleteService(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        vendorService.deleteService(principal, id);
    }

    @PutMapping("/vendors/me/availability")
    @PreAuthorize("hasAuthority('ROLE_VENDOR')")
    public void setAvailability(@AuthenticationPrincipal UserPrincipal principal,
                                @Valid @RequestBody AvailabilityRequest request) {
        vendorService.setAvailability(principal, request);
    }

    @GetMapping("/vendors/{id}/reviews")
    public List<ReviewResponse> reviews(@PathVariable Long id) {
        return vendorService.reviews(id);
    }

    @GetMapping("/admin/vendors/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<VendorSummary> pendingVendors() {
        return vendorService.pendingVendors();
    }

    @PatchMapping("/admin/vendors/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public VendorSummary changeVendorStatus(@PathVariable Long id,
                                            @Valid @RequestBody VendorStatusRequest request,
                                            @AuthenticationPrincipal UserPrincipal admin) {
        return vendorService.changeStatus(id, request.status(), admin);
    }
}
