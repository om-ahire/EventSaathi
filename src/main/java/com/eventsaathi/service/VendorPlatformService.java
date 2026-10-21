package com.eventsaathi.service;

import com.eventsaathi.dto.ApiDtos.*;
import com.eventsaathi.entity.*;
import com.eventsaathi.entity.enums.RoleName;
import com.eventsaathi.entity.enums.VendorStatus;
import com.eventsaathi.exception.BusinessException;
import com.eventsaathi.exception.ResourceNotFoundException;
import com.eventsaathi.repository.*;
import com.eventsaathi.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class VendorPlatformService {
    private final VendorRepository vendorRepository;
    private final VendorCategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final VendorAvailabilityRepository availabilityRepository;

    public VendorPlatformService(VendorRepository vendorRepository, VendorCategoryRepository categoryRepository,
                                CityRepository cityRepository, UserRepository userRepository,
                                RoleRepository roleRepository, VendorServiceRepository vendorServiceRepository,
                                ReviewRepository reviewRepository, NotificationRepository notificationRepository,
                                VendorAvailabilityRepository availabilityRepository) {
        this.vendorRepository = vendorRepository;
        this.categoryRepository = categoryRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.vendorServiceRepository = vendorServiceRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional(readOnly = true)
    public Page<VendorSummary> search(Long categoryId, Long cityId, BigDecimal maxPrice, String query,
                                     int page, int size, String sort, String direction) {
        String sortField = switch (sort == null ? "rating" : sort) {
            case "price" -> "basePrice";
            case "name" -> "businessName";
            case "createdAt" -> "createdAt";
            default -> "rating";
        };
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50),
                Sort.by(sortDirection, sortField).and(Sort.by(Sort.Direction.ASC, "id")));

        Specification<Vendor> specification = (root, criteria, builder) -> {
            List<Predicate> filters = new ArrayList<>();
            filters.add(builder.equal(root.get("status"), VendorStatus.APPROVED));
            if (categoryId != null) filters.add(builder.equal(root.get("category").get("id"), categoryId));
            if (cityId != null) filters.add(builder.equal(root.get("city").get("id"), cityId));
            if (maxPrice != null) filters.add(builder.or(
                    builder.isNull(root.get("basePrice")),
                    builder.lessThanOrEqualTo(root.get("basePrice"), maxPrice)));
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                filters.add(builder.or(
                        builder.like(builder.lower(root.get("businessName")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)));
            }
            return builder.and(filters.toArray(Predicate[]::new));
        };
        return vendorRepository.findAll(specification, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public VendorSummary getPublicVendor(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .filter(item -> item.getStatus() == VendorStatus.APPROVED)
                .orElseThrow(() -> ResourceNotFoundException.of("Vendor", "id", id));
        return toSummary(vendor);
    }

    @Transactional
    public VendorSummary createProfile(UserPrincipal principal, VendorProfileRequest request) {
        User user = getUser(principal.getId());
        if (vendorRepository.findByUserId(user.getId()).isPresent()) {
            throw new BusinessException("This account already has a vendor profile");
        }
        VendorCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Vendor category", "id", request.categoryId()));
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> ResourceNotFoundException.of("City", "id", request.cityId()));
        Vendor vendor = new Vendor(user, request.businessName().trim(), category, city);
        applyProfile(vendor, request);
        Role vendorRole = roleRepository.findByName(RoleName.ROLE_VENDOR)
                .orElseThrow(() -> new IllegalStateException("Vendor role has not been initialized"));
        user.addRole(vendorRole);
        userRepository.save(user);
        return toSummary(vendorRepository.save(vendor));
    }

    @Transactional
    public VendorSummary updateProfile(UserPrincipal principal, VendorProfileRequest request) {
        Vendor vendor = getOwnedVendor(principal.getId());
        if (vendor.getStatus() == VendorStatus.SUSPENDED) {
            throw new BusinessException("A suspended vendor profile cannot be changed");
        }
        VendorCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Vendor category", "id", request.categoryId()));
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> ResourceNotFoundException.of("City", "id", request.cityId()));
        vendor.setCategory(category);
        vendor.setCity(city);
        applyProfile(vendor, request);
        return toSummary(vendor);
    }

    @Transactional(readOnly = true)
    public VendorSummary myProfile(UserPrincipal principal) {
        return toSummary(getOwnedVendor(principal.getId()));
    }

    @Transactional
    public VendorServiceResponse addService(UserPrincipal principal, VendorServiceRequest request) {
        Vendor vendor = getOwnedVendor(principal.getId());
        if (vendor.getStatus() == VendorStatus.SUSPENDED || vendor.getStatus() == VendorStatus.REJECTED) {
            throw new BusinessException("This vendor profile cannot publish services");
        }
        VendorService service = new VendorService(vendor, request.name().trim(), request.description(),
                request.price(), request.maxGuests(), request.durationHours());
        return toServiceResponse(vendorServiceRepository.save(service));
    }

    @Transactional
    public void deleteService(UserPrincipal principal, Long serviceId) {
        Vendor vendor = getOwnedVendor(principal.getId());
        VendorService service = vendorServiceRepository.findByIdAndVendorId(serviceId, vendor.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Vendor service", "id", serviceId));
        vendorServiceRepository.delete(service);
    }

    @Transactional
    public void setAvailability(UserPrincipal principal, AvailabilityRequest request) {
        Vendor vendor = getOwnedVendor(principal.getId());
        VendorAvailability availability = availabilityRepository
                .findByVendorIdAndAvailableDate(vendor.getId(), request.date())
                .orElseGet(() -> new VendorAvailability(vendor, request.date(), request.available(), request.notes()));
        availability.setAvailable(request.available());
        availability.setNotes(request.notes());
        availabilityRepository.save(availability);
    }

    @Transactional(readOnly = true)
    public List<VendorSummary> pendingVendors() {
        return vendorRepository.findByStatusOrderByCreatedAtAsc(VendorStatus.PENDING_APPROVAL)
                .stream().map(this::toSummary).toList();
    }

    @Transactional
    public VendorSummary changeStatus(Long vendorId, VendorStatus status, UserPrincipal admin) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Vendor", "id", vendorId));
        if (status == VendorStatus.PENDING_APPROVAL) {
            throw new BusinessException("A vendor cannot be moved back to pending approval");
        }
        if ((vendor.getStatus() == VendorStatus.REJECTED || vendor.getStatus() == VendorStatus.SUSPENDED)
                && status != VendorStatus.APPROVED && status != VendorStatus.SUSPENDED) {
            throw new BusinessException("Invalid vendor status transition");
        }
        vendor.setStatus(status);
        if (status == VendorStatus.APPROVED) {
            vendor.setApprovedAt(LocalDateTime.now());
            vendor.setApprovedById(admin.getId());
        }
        notificationRepository.save(new Notification(vendor.getUser(),
                "Vendor profile " + status.name().toLowerCase(Locale.ROOT),
                "Your vendor profile \"" + vendor.getBusinessName() + "\" is now " + status.name() + ".",
                "VENDOR_" + status.name()));
        return toSummary(vendor);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> reviews(Long vendorId) {
        if (!vendorRepository.existsById(vendorId)) {
            throw ResourceNotFoundException.of("Vendor", "id", vendorId);
        }
        return reviewRepository.findByVendorIdOrderByCreatedAtDesc(vendorId).stream()
                .map(review -> new ReviewResponse(review.getId(), review.getBooking().getId(), vendorId,
                        review.getCustomer().getName(), review.getRating(), review.getComment(),
                        review.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Vendor getOwnedVendor(Long userId) {
        return vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Create a vendor profile before managing vendor services"));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", id));
    }

    private void applyProfile(Vendor vendor, VendorProfileRequest request) {
        vendor.setBusinessName(request.businessName().trim());
        vendor.setDescription(request.description());
        vendor.setBasePrice(request.basePrice());
        vendor.setAddress(request.address());
        vendor.setContactEmail(request.contactEmail());
        vendor.setContactPhone(request.contactPhone());
        vendor.setWebsiteUrl(request.websiteUrl());
    }

    public VendorSummary toSummary(Vendor vendor) {
        List<VendorServiceResponse> services = vendor.getServices().stream()
                .filter(VendorService::isActive).map(this::toServiceResponse).toList();
        String imageUrl = vendor.getImages().stream()
                .filter(VendorImage::isPrimary)
                .map(VendorImage::getImageUrl)
                .findFirst()
                .orElse(vendor.getImages().isEmpty() ? null : vendor.getImages().get(0).getImageUrl());
        return new VendorSummary(vendor.getId(), vendor.getBusinessName(), vendor.getDescription(),
                vendor.getCategory().getName(), vendor.getCategory().getId(),
                vendor.getCity().getName(), vendor.getCity().getId(), vendor.getBasePrice(),
                vendor.getRating(), vendor.getTotalReviews(), vendor.getStatus(), vendor.getAddress(),
                vendor.getContactEmail(), vendor.getContactPhone(), vendor.getWebsiteUrl(), imageUrl, services);
    }

    private VendorServiceResponse toServiceResponse(VendorService service) {
        return new VendorServiceResponse(service.getId(), service.getName(), service.getDescription(),
                service.getPrice(), service.getMaxGuests(), service.getDurationHours());
    }
}
