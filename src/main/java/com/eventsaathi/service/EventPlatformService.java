package com.eventsaathi.service;

import com.eventsaathi.dto.ApiDtos.EventRequest;
import com.eventsaathi.dto.ApiDtos.EventResponse;
import com.eventsaathi.entity.*;
import com.eventsaathi.entity.enums.BookingStatus;
import com.eventsaathi.entity.enums.EventStatus;
import com.eventsaathi.exception.BusinessException;
import com.eventsaathi.exception.ResourceNotFoundException;
import com.eventsaathi.repository.CityRepository;
import com.eventsaathi.repository.EventRepository;
import com.eventsaathi.repository.EventTypeRepository;
import com.eventsaathi.repository.UserRepository;
import com.eventsaathi.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventPlatformService {
    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    public EventPlatformService(EventRepository eventRepository, EventTypeRepository eventTypeRepository,
                                CityRepository cityRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list(UserPrincipal principal) {
        return eventRepository.findByUserIdOrderByEventDateAsc(principal.getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public EventResponse create(UserPrincipal principal, EventRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", principal.getId()));
        Event event = new Event(user, request.name().trim(), request.eventDate());
        applyRequest(event, request);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(UserPrincipal principal, Long eventId, EventRequest request) {
        Event event = findOwned(principal.getId(), eventId);
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessException("A cancelled or completed event cannot be changed");
        }
        event.setName(request.name().trim());
        event.setEventDate(request.eventDate());
        applyRequest(event, request);
        return toResponse(event);
    }

    @Transactional
    public void cancel(UserPrincipal principal, Long eventId) {
        Event event = findOwned(principal.getId(), eventId);
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException("This event is already completed or cancelled");
        }
        boolean hasActiveBookings = event.getBookings().stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.PENDING
                        || booking.getStatus() == BookingStatus.CONFIRMED);
        if (hasActiveBookings) {
            throw new BusinessException("Cancel or resolve all active bookings before cancelling the event");
        }
        event.setStatus(EventStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public Event findOwned(Long userId, Long eventId) {
        return eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Event", "id", eventId));
    }

    public EventResponse toResponse(Event event) {
        return new EventResponse(event.getId(), event.getName(), event.getEventDate(),
                event.getEventType() == null ? null : event.getEventType().getName(),
                event.getEventType() == null ? null : event.getEventType().getId(),
                event.getCity() == null ? null : event.getCity().getName(),
                event.getCity() == null ? null : event.getCity().getId(),
                event.getGuestCount(), event.getTotalBudget(), event.getDescription(), event.getStatus().name());
    }

    private void applyRequest(Event event, EventRequest request) {
        if (request.eventTypeId() == null) {
            event.setEventType(null);
        } else {
            event.setEventType(eventTypeRepository.findById(request.eventTypeId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Event type", "id", request.eventTypeId())));
        }
        if (request.cityId() == null) {
            event.setCity(null);
        } else {
            event.setCity(cityRepository.findById(request.cityId())
                    .orElseThrow(() -> ResourceNotFoundException.of("City", "id", request.cityId())));
        }
        event.setGuestCount(request.guestCount());
        event.setTotalBudget(request.totalBudget());
        event.setDescription(request.description());
    }
}
