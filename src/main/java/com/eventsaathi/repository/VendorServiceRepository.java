package com.eventsaathi.repository;

import com.eventsaathi.entity.VendorService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorServiceRepository extends JpaRepository<VendorService, Long> {
    List<VendorService> findByVendorIdAndActiveTrue(Long vendorId);
    Optional<VendorService> findByIdAndVendorId(Long id, Long vendorId);
}
