package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.domain.user.User;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.Reservation;
import com.thehorselegend.summs.domain.vehicle.ReservationStatus;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ReservationService{

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;


    public ReservationService(ReservationRepository reservationRepository, VehicleRepository vehicleRepository) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Reservation reserveVehicle(
            UserEntity user,
            Long vehicleId,
            Location startLocation,
            Location endLocation,
            String city,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Check if vehicle is already reserved
        if (reservationRepository.findByVehicleAndStatus(vehicle, ReservationStatus.CONFIRMED).isPresent()) {
            throw new RuntimeException("Vehicle is already reserved");
        }

        // Lock vehicle immediately
        vehicle.setStatus(VehicleStatus.RESERVED);
        vehicleRepository.save(vehicle);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setStartLocation(new LocationEmbeddable(startLocation.latitude(), startLocation.longitude()));
        reservation.setEndLocation(new LocationEmbeddable(endLocation.latitude(), endLocation.longitude()));
        reservation.setCity(city);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservationRepository.save(reservation);
    }
}
