package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.application.service.reservation.ParkingReservationService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingReservationController {
    private static final String RESERVATION_NOT_FOUND_MESSAGE = "Parking reservation not found";
    private static final String CANCEL_NOT_AUTHORIZED_MESSAGE = "User not authorized to cancel this parking reservation";
    private static final String MODIFY_NOT_AUTHORIZED_MESSAGE = "User not authorized to modify this parking reservation";
    private static final String ALREADY_CANCELLED_MESSAGE = "Parking reservation is already cancelled";

    private final ParkingReservationService reservationService;
    private final UserRepository userRepository;

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

        ParkingReservationResponse response =
                reservationService.createReservation(request, resolveAuthenticatedUserId(authentication));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/parking/reservations
     * Returns parking reservations for the authenticated user.
     */
    @GetMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParkingReservationResponse>> getCurrentUserParkingReservations(
            Authentication authentication
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    /**
     * DELETE /api/parking/reservations/{reservationId}
     * Cancels a parking reservation if it belongs to the authenticated user.
     */
    @DeleteMapping("/reservations/{reservationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelParkingReservation(
            @PathVariable Long reservationId,
            Authentication authentication
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        cancelReservation(reservationId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/parking/reservations/{reservationId}/occupy
     * Marks a confirmed parking reservation as active for the authenticated user.
     */
    @PostMapping("/reservations/{reservationId}/occupy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParkingReservationResponse> occupyParkingReservation(
            @PathVariable Long reservationId,
            Authentication authentication
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        return ResponseEntity.ok(updateReservationStatus(() ->
                reservationService.occupyReservation(reservationId, userId)));
    }

    /**
     * POST /api/parking/reservations/{reservationId}/checkout
     * Marks an active parking reservation as completed for the authenticated user.
     */
    @PostMapping("/reservations/{reservationId}/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParkingReservationResponse> checkoutParkingReservation(
            @PathVariable Long reservationId,
            Authentication authentication
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        return ResponseEntity.ok(updateReservationStatus(() ->
                reservationService.checkoutReservation(reservationId, userId)));
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user no longer exists"
                ));

        return user.getId();
    }

    private void cancelReservation(Long reservationId, Long userId) {
        try {
            reservationService.cancelReservation(reservationId, userId);
        } catch (IllegalArgumentException ex) {
            if (RESERVATION_NOT_FOUND_MESSAGE.equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            if (CANCEL_NOT_AUTHORIZED_MESSAGE.equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
            }
            if (ALREADY_CANCELLED_MESSAGE.equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    private ParkingReservationResponse updateReservationStatus(ReservationStatusAction action) {
        try {
            return action.execute();
        } catch (IllegalArgumentException ex) {
            if (RESERVATION_NOT_FOUND_MESSAGE.equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            if (MODIFY_NOT_AUTHORIZED_MESSAGE.equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    private interface ReservationStatusAction {
        ParkingReservationResponse execute();
    }
}
