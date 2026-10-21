package com.eventsaathi.controller;

import com.eventsaathi.dto.ApiDtos.LookupResponse;
import com.eventsaathi.repository.CityRepository;
import com.eventsaathi.repository.EventTypeRepository;
import com.eventsaathi.repository.VendorCategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CityRepository cityRepository;
    private final VendorCategoryRepository categoryRepository;
    private final EventTypeRepository eventTypeRepository;

    public CatalogController(CityRepository cityRepository, VendorCategoryRepository categoryRepository,
                             EventTypeRepository eventTypeRepository) {
        this.cityRepository = cityRepository;
        this.categoryRepository = categoryRepository;
        this.eventTypeRepository = eventTypeRepository;
    }

    @GetMapping("/cities")
    public List<LookupResponse> cities() {
        return cityRepository.findAll(org.springframework.data.domain.Sort.by("name")).stream()
                .map(city -> new LookupResponse(city.getId(), city.getName(), city.getState(), null)).toList();
    }

    @GetMapping("/categories")
    public List<LookupResponse> categories() {
        return categoryRepository.findAll(org.springframework.data.domain.Sort.by("name")).stream()
                .map(category -> new LookupResponse(category.getId(), category.getName(),
                        category.getDescription(), category.getIcon())).toList();
    }

    @GetMapping("/event-types")
    public List<LookupResponse> eventTypes() {
        return eventTypeRepository.findAll(org.springframework.data.domain.Sort.by("name")).stream()
                .map(type -> new LookupResponse(type.getId(), type.getName(), type.getDescription(), type.getIcon()))
                .toList();
    }
}
