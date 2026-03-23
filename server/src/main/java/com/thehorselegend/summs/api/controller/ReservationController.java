package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.VehicleReservationRequest;
import com.thehorselegend.summs.api.dto.VehicleReservationResponse;
import com.thehorselegend.summs.application.service.reservation.VehicleReservationService;
import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.VehicleMapper;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final VehicleReservationService reservationService;
    private final VehicleRepository vehicleRepository;

    public ReservationController(VehicleReservationService reservationService,
                                 VehicleRepository vehicleRepository) {
        this.reservationService = reservationService;
        this.vehicleRepository = vehicleRepository;
    }

    @PostMapping("/vehicles/{vehicleId}/reserve")
    public ResponseEntity<VehicleReservationResponse> reserveVehicle(
            @PathVariable Long vehicleId,
            @RequestBody VehicleReservationRequest request,
            @SessionAttribute("user") UserEntity user
    ) {
        Reservation reservation = reservationService.reserveVehicle(
                user.getId(),
                vehicleId,
                request.getCity(),
                request.getStartDate(),
                request.getEndDate()
        );

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        return ResponseEntity.ok(VehicleReservationResponse.fromDomain(reservation, vehicle));
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<VehicleReservationResponse> cancelReservation(
            @PathVariable Long reservationId,
            @SessionAttribute("user") UserEntity user
    ) {
        reservationService.cancelReservation(reservationId, user.getId());

        Reservation reservation = reservationService.getReservationById(reservationId);

        Vehicle vehicle = vehicleRepository.findById(reservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        return ResponseEntity.ok(VehicleReservationResponse.fromDomain(reservation, vehicle));
    }

    @GetMapping("/users/me/reservations")
    public ResponseEntity<List<VehicleReservationResponse>> getUserReservations(
            @SessionAttribute("user") UserEntity user
    ) {
        List<Reservation> reservations = reservationService.getUserReservations(user.getId());

        List<VehicleReservationResponse> response = reservations.stream()
                .map(r -> {
                    Vehicle vehicle = vehicleRepository.findById(r.getReservableId())
                            .map(VehicleMapper::toDomain)
                            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
                    return VehicleReservationResponse.fromDomain(r, vehicle);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<VehicleReservationResponse> getReservationById(
            @PathVariable Long reservationId
    ) {
        Reservation reservation = reservationService.getReservationById(reservationId);

        Vehicle vehicle = vehicleRepository.findById(reservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        return ResponseEntity.ok(VehicleReservationResponse.fromDomain(reservation, vehicle));
    }
}