package com.eventsaathi.repository;

import com.eventsaathi.entity.Vendor;
import com.eventsaathi.entity.enums.VendorStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long>, JpaSpecificationExecutor<Vendor> {
    Optional<Vendor> findByUserId(Long userId);
    List<Vendor> findByStatusOrderByCreatedAtAsc(VendorStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vendor v where v.id = :id")
    Optional<Vendor> findByIdForUpdate(@Param("id") Long id);
}
