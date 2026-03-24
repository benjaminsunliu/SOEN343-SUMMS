package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.application.service.ParkingReservationService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingReservationController {
     private final ParkingReservationService reservationService;
    private final UserRepository            userRepository;

    /**
     * POST /api/parking/reservations
     * Creates a confirmed parking reservation for the authenticated user.
     * The MockParkingAdapter reads the reservation count to subtract
     * from available spots on future searches.
     */
    @PostMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParkingReservationResponse> createReservation(
            @RequestBody CreateParkingReservationRequest request,
            Authentication authentication) {

        // Resolve the authenticated user's ID from their email/username
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ParkingReservationResponse response =
                reservationService.createReservation(request, user.getId());

        return ResponseEntity.ok(response);
    }
}
