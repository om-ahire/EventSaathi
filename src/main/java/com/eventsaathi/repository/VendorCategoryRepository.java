package com.eventsaathi.repository;

import com.eventsaathi.entity.VendorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorCategoryRepository extends JpaRepository<VendorCategory, Long> {
    Optional<VendorCategory> findByNameIgnoreCase(String name);
}
