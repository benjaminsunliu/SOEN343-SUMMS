package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.EndTripRequest;
import com.thehorselegend.summs.api.dto.StartTripRequest;
import com.thehorselegend.summs.api.dto.TripResponse;
import com.thehorselegend.summs.application.service.RentalLifecycleService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    private final RentalLifecycleService rentalLifecycleService;
    private final UserRepository userRepository;

    public TripController(RentalLifecycleService rentalLifecycleService, UserRepository userRepository) {
        this.rentalLifecycleService = rentalLifecycleService;
        this.userRepository = userRepository;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public TripResponse startTrip(
            @Valid @RequestBody StartTripRequest request,
            Authentication authentication) {
        return rentalLifecycleService.startTrip(resolveAuthenticatedUserId(authentication), request);
    }

    @PostMapping("/{tripId}/end")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public TripResponse endTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody EndTripRequest request,
            Authentication authentication) {
        return rentalLifecycleService.endTrip(resolveAuthenticatedUserId(authentication), tripId, request);
    }

    @GetMapping("/{tripId}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public TripResponse getTripById(@PathVariable Long tripId) {
        return rentalLifecycleService.getTripById(tripId);
    }

    @GetMapping("/active/{citizenId}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public TripResponse getActiveTripForCitizen(@PathVariable Long citizenId) {
        return rentalLifecycleService.getActiveTripForCitizen(citizenId);
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication required.");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user no longer exists."));

        return user.getId();
    }
}
