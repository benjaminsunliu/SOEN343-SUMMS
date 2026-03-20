package com.thehorselegend.summs.domain.vehicle;

public class Bicycle extends Vehicle {

    public Bicycle(
            Long id,
            VehicleStatus status,
            Location location,
            Long providerId,
            Double costPerMinute) {
        super(id, status, location, providerId, costPerMinute);
    }
}
