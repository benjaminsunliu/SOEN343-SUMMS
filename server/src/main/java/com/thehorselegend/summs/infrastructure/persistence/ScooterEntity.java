package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.domain.vehicle.VehicleType;
import jakarta.persistence.*;

@Entity
@Table(name = "scooters")
@DiscriminatorValue("SCOOTER")
@PrimaryKeyJoinColumn(name = "vehicle_id")
public class ScooterEntity extends VehicleEntity {

    @Column(nullable = false)
    private Double maxRange;

    public ScooterEntity() {
    }

    public ScooterEntity(
            Long id,
            VehicleStatus status,
            LocationEmbeddable location,
            Long providerId,
            Double costPerMinute,
            Double maxRange) {
        super(id, VehicleType.SCOOTER, status, location, providerId, costPerMinute);
        this.maxRange = maxRange;
    }

    public Double getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(Double maxRange) {
        this.maxRange = maxRange;
    }
}
