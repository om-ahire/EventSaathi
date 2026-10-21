package com.eventsaathi.repository;

import com.eventsaathi.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookingId(Long bookingId);
    List<Review> findByVendorIdOrderByCreatedAtDesc(Long vendorId);

    @Query("select avg(r.rating) from Review r where r.vendor.id = :vendorId")
    BigDecimal averageRating(@Param("vendorId") Long vendorId);

    long countByVendorId(Long vendorId);
}
