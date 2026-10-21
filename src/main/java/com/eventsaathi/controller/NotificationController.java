package com.eventsaathi.controller;

import com.eventsaathi.dto.ApiDtos.NotificationResponse;
import com.eventsaathi.exception.ResourceNotFoundException;
import com.eventsaathi.repository.NotificationRepository;
import com.eventsaathi.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(item -> new NotificationResponse(item.getId(), item.getTitle(), item.getMessage(),
                        item.getType(), item.isRead(), item.getCreatedAt()))
                .toList();
    }

    @PatchMapping("/{id}/read")
    @Transactional
    public NotificationResponse markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        var notification = notificationRepository.findByIdAndUserId(id, principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", "id", id));
        notification.setRead(true);
        return new NotificationResponse(notification.getId(), notification.getTitle(), notification.getMessage(),
                notification.getType(), notification.isRead(), notification.getCreatedAt());
    }
}
