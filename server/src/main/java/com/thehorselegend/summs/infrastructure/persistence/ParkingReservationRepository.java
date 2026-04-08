package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;

import java.util.List;

public interface ParkingReservationRepository extends JpaRepository<ParkingReservationEntity, Long> {

    /**
     * Retrieves all parking reservations made by a specific user, ordered from newest to oldest.
     * @param userId the ID of the user
     * @return a list of ParkingReservationEntity objects for that user
     */
    List<ParkingReservationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Counts parking reservations for a facility with a specific status.
     * @param facilityId the ID of the parking facility
     * @param status the reservation status to filter by
     * @return the number of matching parking reservations
     */
    long countByReservableIdAndStatus(Long facilityId, ReservationStatus status);
}
