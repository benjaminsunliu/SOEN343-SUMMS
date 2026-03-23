package com.thehorselegend.summs.domain.vehicle;

public class Car extends Vehicle {

    private final String licensePlate;
    private final Integer seatingCapacity;

    public Car(
            Long id,
            VehicleStatus status,
            Location location,
            Long providerId,
            Double costPerMinute,
            String licensePlate,
            Integer seatingCapacity) {
        super(id, VehicleType.CAR, status, location, providerId, costPerMinute);
        this.licensePlate = licensePlate;
        this.seatingCapacity = seatingCapacity;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

}
