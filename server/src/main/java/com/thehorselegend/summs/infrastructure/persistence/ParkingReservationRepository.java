package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingReservationRepository extends JpaRepository<ParkingReservationEntity, Long> {
    int countByFacilityIdAndStatus(Long facilityId, String status);
}
