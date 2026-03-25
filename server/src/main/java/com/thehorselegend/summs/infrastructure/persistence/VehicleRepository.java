package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//  JPA Repository for Vehicle base type.
//  Supports polymorphic queries across all vehicle types.

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    List<VehicleEntity> findByStatus(VehicleStatus status);
    List<VehicleEntity> findByProviderId(Long providerId);

}
