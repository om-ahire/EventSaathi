package com.eventsaathi.repository;

import com.eventsaathi.entity.VendorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface VendorAvailabilityRepository extends JpaRepository<VendorAvailability, Long> {
    Optional<VendorAvailability> findByVendorIdAndAvailableDate(Long vendorId, LocalDate date);
}
