package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.parking.ParkingSpot;
import com.thehorselegend.summs.domain.parking.SpotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByFacilityFacilityIdAndStatus(
            Long facilityId, SpotStatus status);

    int countByFacilityFacilityIdAndStatus(
            Long facilityId, SpotStatus status);
}
