package com.thehorselegend.summs.application.service.reservation;

import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VehicleReservationService extends AbstractReservationService<VehicleEntity> {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;

    public VehicleReservationService(ReservationRepository reservationRepository,
                                     VehicleRepository vehicleRepository) {
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

        Reservation reservation = createReservation(
                vehicle,
                user.getId(),
                startDate,
                endDate
        );

        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setStartLocation(
                new LocationEmbeddable(startLocation.latitude(), startLocation.longitude())
        );
        reservation.setEndLocation(
                new LocationEmbeddable(endLocation.latitude(), endLocation.longitude())
        );
        reservation.setCity(city);

        return reservation;
    }

    @Override
    protected void validateAvailability(VehicleEntity vehicle,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate) {

        if (reservationRepository
                .findByVehicleAndStatus(vehicle, ReservationStatus.CONFIRMED)
                .isPresent()) {
            throw new RuntimeException("Vehicle is already reserved");
        }

        // lock vehicle
        vehicle.setStatus(VehicleStatus.RESERVED);
        vehicleRepository.save(vehicle);
    }

    @Override
    protected Reservation buildReservation(
            VehicleEntity vehicle,
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Reservation reservation = new Reservation();
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservation;
    }

    @Override
    protected Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, UserEntity user) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot cancel someone else's reservation");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        VehicleEntity vehicle = reservation.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        return reservationRepository.save(reservation);
    }
}