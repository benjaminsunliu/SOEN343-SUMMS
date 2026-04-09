package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ParkingReservationRepository extends JpaRepository<ParkingReservationEntity, Long> {
    int countByFacilityIdAndStatus(Long facilityId, ReservationStatus status);

    int countByFacilityIdAndStatusIn(Long facilityId, Set<ReservationStatus> statuses);

    List<ParkingReservationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ParkingReservationEntity> findByFacilityIdInOrderByCreatedAtDesc(List<Long> facilityIds);
}
