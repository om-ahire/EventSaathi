package com.eventsaathi.controller;

import com.eventsaathi.dto.ApiDtos.EventRequest;
import com.eventsaathi.dto.ApiDtos.EventResponse;
import com.eventsaathi.security.UserPrincipal;
import com.eventsaathi.service.EventPlatformService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_VENDOR', 'ROLE_ADMIN')")
public class EventController {
    private final EventPlatformService eventService;

    public EventController(EventPlatformService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return eventService.list(principal);
    }

    @PostMapping
    public EventResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                @Valid @RequestBody EventRequest request) {
        return eventService.create(principal, request);
    }

    @PutMapping("/{id}")
    public EventResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                               @Valid @RequestBody EventRequest request) {
        return eventService.update(principal, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        eventService.cancel(principal, id);
    }
}
