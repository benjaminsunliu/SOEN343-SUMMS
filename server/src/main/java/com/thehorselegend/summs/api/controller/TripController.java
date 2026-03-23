package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.EndTripRequest;
import com.thehorselegend.summs.api.dto.StartTripRequest;
import com.thehorselegend.summs.api.dto.TripResponse;
import com.thehorselegend.summs.application.service.RentalLifecycleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    private final RentalLifecycleService rentalLifecycleService;

    public TripController(RentalLifecycleService rentalLifecycleService) {
        this.rentalLifecycleService = rentalLifecycleService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public TripResponse startTrip(@Valid @RequestBody StartTripRequest request) {
        return rentalLifecycleService.startTrip(request);
    }

    @PostMapping("/{tripId}/end")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public TripResponse endTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody EndTripRequest request) {
        return rentalLifecycleService.endTrip(tripId, request);
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
}
