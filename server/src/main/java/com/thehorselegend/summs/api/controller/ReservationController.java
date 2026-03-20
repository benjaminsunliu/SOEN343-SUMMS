package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.ReservationRequest;
import com.thehorselegend.summs.domain.user.User;
import com.thehorselegend.summs.domain.vehicle.Reservation;
import com.thehorselegend.summs.application.service.ReservationService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
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
                request.startLocation(),
                request.endLocation(),
                request.city(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.ok(reservation);
    }
}
