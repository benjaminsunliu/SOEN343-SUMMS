package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.Reservation;
import com.thehorselegend.summs.domain.vehicle.ReservationStatus;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByVehicleAndStatus(VehicleEntity vehicle, ReservationStatus status);
}
