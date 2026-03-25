package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.VehicleReservationRequest;
import com.thehorselegend.summs.api.dto.VehicleReservationResponse;
import com.thehorselegend.summs.application.service.reservation.VehicleReservationService;
import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.vehicle.Location;
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

    /*
    POST /api/vehicles/{vehicleId}/reserve
    Creates a new vehicle reservation for the given vehicle ID using the provided request data (locations, dates, city) for the currently logged-in user.
    */
    @PostMapping("/vehicles/{vehicleId}/reserve")
    public ResponseEntity<VehicleReservationResponse> reserveVehicle(
            @PathVariable Long vehicleId,
            @RequestBody VehicleReservationRequest request,
            @SessionAttribute("user") UserEntity user
    ) {
        // Pass start/end LocationDto from request
        VehicleReservation reservation = (VehicleReservation) reservationService.reserveVehicle(
                user.getId(),
                vehicleId,
                request.getCity(),
                request.getStartLocation(),
                request.getEndLocation(),
                request.getStartDate(),
                request.getEndDate()
        );

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        return ResponseEntity.ok(VehicleReservationResponse.fromDomain(reservation, vehicle));
    }

    /*
    POST /api/vehicle-reservations/{reservationId}/cancel
    Cancels an existing vehicle reservation for the given reservation ID, ensuring it belongs to the current user.
    */
    @PostMapping("/reservation/{reservationId}/cancel")
    public ResponseEntity<VehicleReservationResponse> cancelVehicleReservation(
            @PathVariable Long reservationId,
            @SessionAttribute("user") UserEntity user
    ) {
        // Cancel the reservation using the service
        reservationService.cancelReservation(reservationId, user.getId());

        Reservation reservation = reservationService.getReservationById(reservationId);
        if (!(reservation instanceof VehicleReservation vehicleReservation)) {
            throw new IllegalArgumentException("Reservation is not a vehicle reservation");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleReservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Return DTO
        return ResponseEntity.ok(VehicleReservationResponse.fromDomain(vehicleReservation, vehicle));
    }

    /*
    GET /api/users/me/vehicle-reservations
    Retrieves all vehicle reservations associated with the currently logged-in user.
    */
    @GetMapping("/users/me/vehicle-reservations")
    public ResponseEntity<List<VehicleReservationResponse>> getUserVehicleReservations(
            @SessionAttribute("user") UserEntity user
    ) {
        // Fetch all reservations of the user
        List<Reservation> reservations = reservationService.getUserReservations(user.getId());

        // Filter only vehicle reservations
        List<VehicleReservationResponse> response = reservations.stream()
                .filter(reservation -> reservation instanceof VehicleReservation)
                .map(reservation -> {
                    VehicleReservation vehicleReservation = (VehicleReservation) reservation;

                    // Fetch vehicle info
                    Vehicle vehicle = vehicleRepository.findById(vehicleReservation.getReservableId())
                            .map(VehicleMapper::toDomain)
                            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

                    // Convert to DTO
                    return VehicleReservationResponse.fromDomain(vehicleReservation, vehicle);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /*
    GET /api/vehicle-reservations/{reservationId}
    Retrieves a specific vehicle reservation by its ID, including associated vehicle details.
    */
    @GetMapping("/vehicle-reservations/{reservationId}")
    public ResponseEntity<VehicleReservationResponse> getVehicleReservationById(
            @PathVariable Long reservationId
    ) {
        // Fetch reservation from service
        Reservation reservation = reservationService.getReservationById(reservationId);

        // Ensure it’s a VehicleReservation
        if (!(reservation instanceof VehicleReservation vehicleReservation)) {
            throw new IllegalArgumentException("Reservation is not a vehicle reservation");
        }

        // Fetch the associated vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleReservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Map to DTO
        return ResponseEntity.ok(VehicleReservationResponse.fromDomain(vehicleReservation, vehicle));
    }
}