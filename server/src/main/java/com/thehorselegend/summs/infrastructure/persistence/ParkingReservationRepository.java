package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingReservationRepository extends JpaRepository<ParkingReservationEntity, Long> {
    int countByFacilityIdAndStatus(Long facilityId, ReservationStatus status);

    List<ParkingReservationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
