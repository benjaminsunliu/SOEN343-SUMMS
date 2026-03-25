package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.domain.vehicle.VehicleType;
import jakarta.persistence.*;

@Entity
@Table(name = "cars")
@DiscriminatorValue("CAR")
@PrimaryKeyJoinColumn(name = "vehicle_id")
public class CarEntity extends VehicleEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(nullable = false)
    private Integer seatingCapacity;

    public CarEntity() {
    }

    public CarEntity(
            Long id,
            VehicleStatus status,
            LocationEmbeddable location,
            Long providerId,
            Double costPerMinute,
            String licensePlate,
            Integer seatingCapacity) {
        super(id, status, location, providerId, costPerMinute);
        this.licensePlate = licensePlate;
        this.seatingCapacity = seatingCapacity;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }
}
