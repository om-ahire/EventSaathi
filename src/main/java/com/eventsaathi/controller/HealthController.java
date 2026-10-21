package com.eventsaathi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HealthController – a simple "is the app alive?" endpoint.
 *
 * WHY /api/v1/health and not Spring Actuator?
 *   Actuator adds significant classpath weight and many endpoints. For Phase 0
 *   we just need proof the server started. We can add Actuator in Phase 7.
 *
 * WHY ResponseEntity<Map<...>> instead of a DTO?
 *   For a transient diagnostic endpoint, a map is acceptable. All real business
 *   endpoints will use proper response DTOs (Phase 2+).
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "EventSaathi");
        response.put("version", "1.0.0-SNAPSHOT");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "🎉 EventSaathi backend is running!");
        return ResponseEntity.ok(response);
    }
}
