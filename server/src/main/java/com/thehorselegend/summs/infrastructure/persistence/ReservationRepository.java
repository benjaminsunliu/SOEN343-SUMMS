package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    /**
     * Retrieves all reservations made by a specific user.
     * @param userId the ID of the user
     * @return a list of ReservationEntity objects for that user
     */
    List<ReservationEntity> findByUserId(Long userId);

    /**
     * Retrieves all reservations for a specific reservable item (e.g., vehicle, room).
     * @param reservableId the ID of the reservable item
     * @return a list of ReservationEntity objects for that item
     */
    List<ReservationEntity> findByReservableId(Long reservableId);

    /**
     * Retrieves all reservations with a specific status (e.g., CONFIRMED, CANCELLED).
     * @param status the ReservationStatus to filter by
     * @return a list of ReservationEntity objects with the given status
     */
    List<ReservationEntity> findByStatus(ReservationStatus status);
}