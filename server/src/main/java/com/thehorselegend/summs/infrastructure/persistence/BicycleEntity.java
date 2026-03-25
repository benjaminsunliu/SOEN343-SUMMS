package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.domain.vehicle.VehicleType;
import jakarta.persistence.*;

@Entity
@Table(name = "bicycles")
@DiscriminatorValue("BICYCLE")
@PrimaryKeyJoinColumn(name = "vehicle_id")
public class BicycleEntity extends VehicleEntity {

    public BicycleEntity() {
    }

    public BicycleEntity(
            Long id,
            VehicleStatus status,
            LocationEmbeddable location,
            Long providerId,
            Double costPerMinute) {
        super(id, status, location, providerId, costPerMinute);
    }
}
