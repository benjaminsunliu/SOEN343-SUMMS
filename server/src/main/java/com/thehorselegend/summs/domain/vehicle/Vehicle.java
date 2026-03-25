package com.thehorselegend.summs.domain.vehicle;

public abstract class Vehicle {

    private final Long id;
    private final VehicleType type;
    private VehicleStatus status;
    private Location location;
    private final Long providerId;
    private Double costPerMinute;

    // Constructor's protected as it's used primarily by 
    // the domain vehicle factory and persistence mappers.
    protected Vehicle(
            Long id,
            VehicleType type,
            VehicleStatus status,
            Location location,
            Long providerId,
            Double costPerMinute) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.location = location;
        this.providerId = providerId;
        this.costPerMinute = costPerMinute;
    }

    public VehicleType getType() {
        return type;
    }

    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }

    public void reserve() {
        if (status != VehicleStatus.AVAILABLE) throw new IllegalStateException("Vehicle not available");
        this.status = VehicleStatus.RESERVED;
    }

    public void makeAvailable() {
        this.status = VehicleStatus.AVAILABLE;
    }

    public void release(Location newLocation) {
        this.status = VehicleStatus.AVAILABLE;
        this.location = newLocation;
    }

    public Long getId() {
        return id;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public Long getProviderId() {
        return providerId;
    }

    public Double getCostPerMinute() {
        return costPerMinute;
    }

    public void setStatus(Double costPerMinute) {
        this.costPerMinute = costPerMinute;
    }

}
