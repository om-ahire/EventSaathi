package com.eventsaathi.service;

import com.eventsaathi.dto.ApiDtos.*;
import com.eventsaathi.entity.*;
import com.eventsaathi.entity.enums.BookingStatus;
import com.eventsaathi.entity.enums.EventStatus;
import com.eventsaathi.entity.enums.PaymentStatus;
import com.eventsaathi.exception.BusinessException;
import com.eventsaathi.exception.ResourceNotFoundException;
import com.eventsaathi.repository.*;
import com.eventsaathi.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingPlatformService {
    private static final List<BookingStatus> OCCUPYING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final EventPlatformService eventService;
    private final VendorPlatformService vendorService;
    private final VendorRepository vendorRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final VendorAvailabilityRepository availabilityRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;

    public BookingPlatformService(BookingRepository bookingRepository, EventPlatformService eventService,
                                  VendorPlatformService vendorService, VendorRepository vendorRepository,
                                  VendorServiceRepository vendorServiceRepository,
                                  VendorAvailabilityRepository availabilityRepository,
                                  NotificationRepository notificationRepository, ReviewRepository reviewRepository,
                                  PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.eventService = eventService;
        this.vendorService = vendorService;
        this.vendorRepository = vendorRepository;
        this.vendorServiceRepository = vendorServiceRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationRepository = notificationRepository;
        this.reviewRepository = reviewRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public BookingResponse create(UserPrincipal principal, BookingRequest request) {
        Event event = eventService.findOwned(principal.getId(), request.eventId());
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessException("Bookings cannot be added to a cancelled or completed event");
        }
        Vendor vendor = vendorRepository.findByIdForUpdate(request.vendorId())
                .orElseThrow(() -> ResourceNotFoundException.of("Vendor", "id", request.vendorId()));
        if (vendor.getStatus() != com.eventsaathi.entity.enums.VendorStatus.APPROVED) {
            throw new BusinessException("Only approved vendors can be booked");
        }
        LocalDate date = event.getEventDate();
        if (availabilityRepository.findByVendorIdAndAvailableDate(vendor.getId(), date)
                .filter(item -> !item.isAvailable()).isPresent()) {
            throw new BusinessException("This vendor is unavailable on the selected date");
        }
        if (bookingRepository.existsByVendorIdAndBookingDateAndStatusIn(
                vendor.getId(), date, OCCUPYING_STATUSES)) {
            throw new BusinessException("This vendor already has an active booking on the selected date");
        }

        VendorService service = null;
        BigDecimal amount = vendor.getBasePrice();
        if (request.vendorServiceId() != null) {
            service = vendorServiceRepository.findByIdAndVendorId(request.vendorServiceId(), vendor.getId())
                    .filter(VendorService::isActive)
                    .orElseThrow(() -> ResourceNotFoundException.of(
                            "Active vendor service", "id", request.vendorServiceId()));
            amount = service.getPrice();
            if (event.getGuestCount() != null && service.getMaxGuests() != null
                    && event.getGuestCount() > service.getMaxGuests()) {
                throw new BusinessException("The selected service package does not support this guest count");
            }
        }
        Booking booking = new Booking(event, vendor, event.getUser(), date);
        booking.setVendorService(service);
        booking.setTotalAmount(amount);
        booking.setNotes(request.notes());
        Booking saved = bookingRepository.save(booking);
        notificationRepository.save(new Notification(vendor.getUser(), "New booking request",
                event.getName() + " has a new booking request for " + date + ".",
                "NEW_BOOKING"));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> customerBookings(UserPrincipal principal) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> vendorBookings(UserPrincipal principal) {
        Vendor vendor = vendorService.getOwnedVendor(principal.getId());
        return bookingRepository.findByVendorIdOrderByCreatedAtDesc(vendor.getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public BookingResponse decide(UserPrincipal principal, Long id, boolean accept, String note) {
        Vendor vendor = vendorService.getOwnedVendor(principal.getId());
        Booking booking = getBooking(id);
        if (!booking.getVendor().getId().equals(vendor.getId())) {
            throw new BusinessException("You may only respond to bookings for your own vendor profile");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be accepted or rejected");
        }
        booking.setStatus(accept ? BookingStatus.CONFIRMED : BookingStatus.REJECTED);
        booking.setVendorNote(note);
        if (accept) booking.getEvent().setStatus(EventStatus.ACTIVE);
        notifyCustomer(booking, accept ? "Booking confirmed" : "Booking declined",
                accept ? "Your booking with " + vendor.getBusinessName() + " has been confirmed."
                        : "Your booking with " + vendor.getBusinessName() + " was declined.",
                accept ? "BOOKING_CONFIRMED" : "BOOKING_REJECTED");
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(UserPrincipal principal, Long id, String note) {
        Booking booking = getBooking(id);
        boolean isCustomer = booking.getCustomer().getId().equals(principal.getId());
        boolean isVendorOwner = booking.getVendor().getUser().getId().equals(principal.getId());
        if (!isCustomer && !isVendorOwner) {
            throw new BusinessException("You may only cancel your own bookings");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only pending or confirmed bookings can be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        if (note != null && !note.isBlank()) booking.setVendorNote(note);
        User recipient = isCustomer ? booking.getVendor().getUser() : booking.getCustomer();
        notificationRepository.save(new Notification(recipient, "Booking cancelled",
                "The booking for " + booking.getEvent().getName() + " has been cancelled.",
                "BOOKING_CANCELLED"));
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse complete(UserPrincipal principal, Long id) {
        Vendor vendor = vendorService.getOwnedVendor(principal.getId());
        Booking booking = getBooking(id);
        if (!booking.getVendor().getId().equals(vendor.getId())) {
            throw new BusinessException("You may only complete bookings for your own vendor profile");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED || booking.getBookingDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Only confirmed bookings whose event date has passed can be completed");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        return toResponse(booking);
    }

    @Transactional
    public ReviewResponse review(UserPrincipal principal, Long bookingId, ReviewRequest request) {
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", "id", bookingId));
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("A review can only be added after the booking is completed");
        }
        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw new BusinessException("This booking already has a review");
        }
        Review review = reviewRepository.save(new Review(booking, booking.getCustomer(), booking.getVendor(),
                request.rating(), request.comment()));
        reviewRepository.flush();
        BigDecimal average = reviewRepository.averageRating(booking.getVendor().getId());
        booking.getVendor().setRating(average == null ? BigDecimal.ZERO : average.setScale(2, java.math.RoundingMode.HALF_UP));
        booking.getVendor().setTotalReviews((int) reviewRepository.countByVendorId(booking.getVendor().getId()));
        return new ReviewResponse(review.getId(), bookingId, booking.getVendor().getId(),
                booking.getCustomer().getName(), review.getRating(), review.getComment(), review.getCreatedAt());
    }

    @Transactional
    public PaymentResponse simulatePayment(UserPrincipal principal, Long bookingId, PaymentRequest request) {
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", "id", bookingId));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Payment is available only for confirmed bookings");
        }
        if (booking.getTotalAmount() == null || booking.getTotalAmount().signum() <= 0) {
            throw new BusinessException("This booking does not have a payable amount");
        }
        if (paymentRepository.findByBookingId(bookingId).filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .isPresent()) {
            throw new BusinessException("This booking has already been paid");
        }
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseGet(() -> new Payment(booking, booking.getTotalAmount(), request.paymentMethod().trim()));
        payment.setPaymentMethod(request.paymentMethod().trim());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId("SIMULATED-" + bookingId + "-" + System.currentTimeMillis());
        Payment saved = paymentRepository.save(payment);
        return new PaymentResponse(saved.getId(), bookingId, saved.getAmount(), saved.getStatus().name(),
                saved.getPaymentMethod(), saved.getPaidAt());
    }

    private Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", "id", id));
    }

    private void notifyCustomer(Booking booking, String title, String message, String type) {
        notificationRepository.save(new Notification(booking.getCustomer(), title, message, type));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getEvent().getId(), booking.getEvent().getName(),
                booking.getVendor().getId(), booking.getVendor().getBusinessName(),
                booking.getCustomer().getId(), booking.getCustomer().getName(),
                booking.getBookingDate(), booking.getStatus(), booking.getTotalAmount(),
                booking.getNotes(), booking.getVendorNote(), booking.getCreatedAt());
    }
}
