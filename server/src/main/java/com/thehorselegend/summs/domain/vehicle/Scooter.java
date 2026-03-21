package com.thehorselegend.summs.domain.vehicle;

public class Scooter extends Vehicle {
    
    private Double maxRange;

    public Scooter(
            Long id,
            VehicleStatus status,
            Location location,
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
