package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//  JPA Repository for Vehicle base type.
//  Supports polymorphic queries across all vehicle types.

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    List<VehicleEntity> findByStatus(VehicleStatus status);
    List<VehicleEntity> findByProviderId(Long providerId);
    @Query("SELECT v FROM VehicleEntity v " +
            "WHERE v.status = com.thehorselegend.summs.domain.vehicle.VehicleStatus.AVAILABLE " +
            "AND FUNCTION('SQRT', POWER(v.location.latitude - :lat, 2) + POWER(v.location.longitude - :lon, 2)) <= :radius")
    List<VehicleEntity> findAvailableNear(@Param("lat") double lat,
                                          @Param("lon") double lon,
                                          @Param("radius") double radius);
    default List<VehicleEntity> findAvailable() {
        return findByStatus(VehicleStatus.AVAILABLE);
    }
}
