package com.eventsaathi.repository;

import com.eventsaathi.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCaseAndStateIgnoreCase(String name, String state);
}
