package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripRepository extends JpaRepository<TripEntity, Long> {
    Optional<TripEntity> findByVehicleIdAndEndTimeIsNull(Long vehicleId);
    Optional<TripEntity> findByCitizenIdAndEndTimeIsNull(Long citizenId);
    Optional<TripEntity> findByReservationId(Long reservationId);
}
