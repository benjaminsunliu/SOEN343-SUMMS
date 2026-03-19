package com.thehorselegend.summs.domain.vehicle;

public abstract class Vehicle {
    
    private Long id;
    private VehicleType type;
    private VehicleStatus status;
    private Location location;
    private Long providerId;
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

    // Getters
    public Long getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
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

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCostPerMinute(Double costPerMinute) {
        this.costPerMinute = costPerMinute;
    }
}
