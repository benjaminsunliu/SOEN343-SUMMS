package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TripRepository extends JpaRepository<TripEntity, Long> {
    Optional<TripEntity> findByVehicleIdAndEndTimeIsNull(Long vehicleId);
    Optional<TripEntity> findByCitizenIdAndEndTimeIsNull(Long citizenId);
    Optional<TripEntity> findByReservationId(Long reservationId);

    @Query("SELECT COALESCE(SUM(t.co2SavedKg), 0.0) FROM TripEntity t " +
            "WHERE t.citizenId = :citizenId AND t.co2SavedKg IS NOT NULL")
    Double sumCo2SavedByUserId(@Param("citizenId") Long citizenId);

    @Query("SELECT COALESCE(SUM(t.co2SavedKg), 0.0) FROM TripEntity t " +
            "WHERE t.co2SavedKg IS NOT NULL")
    Double sumCo2SavedGlobally();

    @Query("SELECT COUNT(t) FROM TripEntity t " +
            "WHERE t.citizenId = :citizenId AND t.co2SavedKg > 0.0 AND t.endTime IS NOT NULL")
    Long countSustainableTripsByUserId(@Param("citizenId") Long citizenId);
}
