package com.eventsaathi.controller;

import com.eventsaathi.dto.ApiDtos.RecommendationRequest;
import com.eventsaathi.dto.ApiDtos.RecommendationResponse;
import com.eventsaathi.security.UserPrincipal;
import com.eventsaathi.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public RecommendationResponse recommend(@Valid @RequestBody RecommendationRequest request,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        return recommendationService.recommend(request, principal);
    }
}
