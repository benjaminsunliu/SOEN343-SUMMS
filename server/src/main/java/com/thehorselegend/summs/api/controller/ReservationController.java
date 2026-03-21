package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.ReservationRequest;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.application.service.reservation.VehicleReservationService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final VehicleReservationService reservationService;

    public ReservationController(VehicleReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/vehicles/{vehicleId}/reserve")
    public ResponseEntity<?> reserveVehicle(
            @PathVariable Long vehicleId,
            @RequestBody ReservationRequest request,
            HttpSession session
    ) {

        UserEntity user = (UserEntity) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not logged in");
        }

        Reservation reservation = reservationService.reserveVehicle(
                user,
                vehicleId,
                new Location(request.startLocation().latitude(), request.startLocation().longitude()),
                new Location(request.endLocation().latitude(), request.endLocation().longitude()),
                request.city(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(
            @PathVariable Long reservationId,
            HttpSession session
    ) {
        UserEntity user = (UserEntity) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not logged in");
        }

        try {
            Reservation cancelled = reservationService.cancelReservation(reservationId, user);
            return ResponseEntity.ok(cancelled);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
