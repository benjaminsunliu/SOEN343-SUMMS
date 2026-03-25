package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.VehicleReservationRequest;
import com.thehorselegend.summs.api.dto.VehicleReservationResponse;
import com.thehorselegend.summs.application.service.reservation.VehicleReservationService;
import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private static final String VEHICLE_NOT_FOUND_MESSAGE = "Vehicle not found";
    private static final String RESERVATION_NOT_FOUND_MESSAGE = "Reservation not found";
    private static final String CANCEL_NOT_AUTHORIZED_MESSAGE = "User not authorized to cancel this reservation";
    private static final String ALREADY_CANCELLED_MESSAGE = "Reservation is already cancelled";

    private final VehicleReservationService reservationService;
    private final UserRepository userRepository;

    public ReservationController(VehicleReservationService reservationService,
                                 UserRepository userRepository) {
        this.reservationService = reservationService;
        this.userRepository = userRepository;
    }

    /**
     * Creates a vehicle reservation for the authenticated user.
     * Endpoint: POST /api/vehicles/{vehicleId}/reservations.
     * Returns 201 Created with the reservation payload and Location header.
     */
    @PostMapping("/vehicles/{vehicleId}/reservations")
    public ResponseEntity<VehicleReservationResponse> createVehicleReservation(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleReservationRequest request,
            Authentication authentication,
            UriComponentsBuilder uriBuilder
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        VehicleReservation reservation = reserveVehicle(userId, vehicleId, request);

        URI location = uriBuilder.path("/api/reservations/{reservationId}")
                .buildAndExpand(reservation.getId())
                .toUri();

        return ResponseEntity.created(location).body(VehicleReservationResponse.fromDomain(reservation));
    }

    /**
     * Cancels an existing reservation owned by the authenticated user.
     * Endpoint: DELETE /api/reservations/{reservationId}.
     * Returns 204 No Content when cancellation succeeds.
     */
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> cancelVehicleReservation(
            @PathVariable Long reservationId,
            Authentication authentication
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        cancelReservation(reservationId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all vehicle reservations for the authenticated user.
     * Endpoint: GET /api/reservations.
     * Returns 200 OK with a reservation list.
     */
    @GetMapping("/reservations")
    public ResponseEntity<List<VehicleReservationResponse>> getCurrentUserReservations(
            Authentication authentication
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        List<VehicleReservationResponse> response = reservationService.getUserReservations(userId).stream()
                .map(this::toVehicleReservation)
                .map(VehicleReservationResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Fetches one reservation by id for the authenticated user.
     * Endpoint: GET /api/reservations/{reservationId}.
     * Returns 200 OK if found and owned by the caller.
     */
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<VehicleReservationResponse> getVehicleReservationById(
            @PathVariable Long reservationId,
            Authentication authentication
    ) {
        Long userId = resolveAuthenticatedUserId(authentication);
        VehicleReservation reservation = fetchVehicleReservationById(reservationId);

        if (!reservation.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this reservation");
        }

        return ResponseEntity.ok(VehicleReservationResponse.fromDomain(reservation));
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Authenticated user no longer exists"));

        return user.getId();
    }

    private VehicleReservation reserveVehicle(Long userId, Long vehicleId, VehicleReservationRequest request) {
        try {
            return (VehicleReservation) reservationService.reserveVehicle(
                    userId,
                    vehicleId,
                    request.getCity(),
                    request.getStartLocation(),
                    request.getEndLocation(),
                    request.getStartDate(),
                    request.getEndDate()
            );
        } catch (IllegalArgumentException ex) {
            if (VEHICLE_NOT_FOUND_MESSAGE.equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
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

    private VehicleReservation fetchVehicleReservationById(Long reservationId) {
        try {
            return toVehicleReservation(reservationService.getReservationById(reservationId));
        } catch (IllegalArgumentException ex) {
            if (RESERVATION_NOT_FOUND_MESSAGE.equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private VehicleReservation toVehicleReservation(Reservation reservation) {
        if (reservation instanceof VehicleReservation vehicleReservation) {
            return vehicleReservation;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle reservation not found");
    }
}
