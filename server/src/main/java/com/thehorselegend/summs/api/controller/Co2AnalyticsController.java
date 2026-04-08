package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.infrastructure.persistence.TripRepository;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * REST Controller for CO₂ emissions analytics.
 * Provides endpoints for tracking CO₂ savings from sustainable mobility options.
 */
@RestController
@RequestMapping("/api/analytics/co2")
@CrossOrigin(origins = "*")
public class Co2AnalyticsController {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public Co2AnalyticsController(TripRepository tripRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/analytics/co2/user/{userId}
     * Retrieve total CO₂ savings and sustainable trip count for a specific user.
     *
     * @param userId the ID of the user
     * @return the total CO₂ saved in kilograms and count of sustainable trips
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'PROVIDER', 'ADMIN')")
    public Co2SavingsResponse getUserCo2Savings(@PathVariable Long userId) {
        Double totalCo2 = tripRepository.sumCo2SavedByUserId(userId);
        if (totalCo2 == null) {
            totalCo2 = 0.0;
        }
        Long sustainableTripCount = tripRepository.countSustainableTripsByUserId(userId);
        if (sustainableTripCount == null) {
            sustainableTripCount = 0L;
        }
        return new Co2SavingsResponse(totalCo2, sustainableTripCount);
    }

    /**
     * GET /api/analytics/co2/global
     * Retrieve total CO₂ savings across all users in the system.
     *
     * @return the total CO₂ saved globally in kilograms
     */
    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('CITIZEN', 'PROVIDER', 'ADMIN')")
    public Co2SavingsResponse getGlobalCo2Savings() {
        Double totalCo2 = tripRepository.sumCo2SavedGlobally();
        if (totalCo2 == null) {
            totalCo2 = 0.0;
        }
        return new Co2SavingsResponse(totalCo2);
    }

    /**
     * Simple DTO for CO₂ savings response
     */
    public static class Co2SavingsResponse {
        private final Double totalCo2SavedKg;
        private final Long sustainableTripCount;

        public Co2SavingsResponse(Double totalCo2SavedKg) {
            this.totalCo2SavedKg = totalCo2SavedKg;
            this.sustainableTripCount = 0L;
        }

        public Co2SavingsResponse(Double totalCo2SavedKg, Long sustainableTripCount) {
            this.totalCo2SavedKg = totalCo2SavedKg;
            this.sustainableTripCount = sustainableTripCount;
        }

        public Double getTotalCo2SavedKg() {
            return totalCo2SavedKg;
        }

        public Long getSustainableTripCount() {
            return sustainableTripCount;
        }
    }
}

