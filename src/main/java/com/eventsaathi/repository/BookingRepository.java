package com.eventsaathi.repository;

import com.eventsaathi.entity.Booking;
import com.eventsaathi.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Booking> findByVendorIdOrderByCreatedAtDesc(Long vendorId);
    Optional<Booking> findByIdAndCustomerId(Long id, Long customerId);
    boolean existsByVendorIdAndBookingDateAndStatusIn(
            Long vendorId, LocalDate date, Collection<BookingStatus> statuses);
    long countByVendorId(Long vendorId);
}
