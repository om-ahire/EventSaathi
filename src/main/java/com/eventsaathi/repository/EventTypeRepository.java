package com.eventsaathi.repository;

import com.eventsaathi.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {
    Optional<EventType> findByNameIgnoreCase(String name);
}
