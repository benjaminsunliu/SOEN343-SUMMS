package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;

import java.util.List;

/**
 * Repository for accessing Reservation entities.
 * Supports both Vehicle and Parking reservations through JPA inheritance.
 */
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    /**
     * Retrieves all reservations made by a specific user.
     * @param userId the ID of the user
     * @return a list of ReservationEntity objects for that user
     */
    List<ReservationEntity> findByUserId(Long userId);

    /**
     * Retrieves all reservations associated with a specific reservable item.
     * @param reservableId the ID of the reservable resource
     * @return a list of ReservationEntity objects linked to that resource
     */
    List<ReservationEntity> findByReservableId(Long reservableId);

    /**
     * Retrieves all reservations with a specific status.
     * @param status the reservation status to filter by
     * @return a list of ReservationEntity objects with the given status
     */
    List<ReservationEntity> findByStatus(ReservationStatus status);
}