package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.domain.vehicle.VehicleType;
import jakarta.persistence.*;

/**
 * Abstract JPA entity representing a vehicle using JOINED table inheritance.
 * - Base table "vehicles" contains common vehicle fields
 * - Each subclass has its own table (bicycles, scooters, cars)
 * - Discriminator column distinguishes vehicle types
 */
@Entity
@Table(name = "vehicles")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "vehicle_type", discriminatorType = DiscriminatorType.STRING)
public abstract class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Embedded
    private LocationEmbeddable location;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private Double costPerMinute;

    public VehicleEntity() {
    }

    public VehicleEntity(
            Long id,
            VehicleStatus status,
            LocationEmbeddable location,
            Long providerId,
            Double costPerMinute) {
        this.id = id;
        this.status = status;
        this.location = location;
        this.providerId = providerId;
        this.costPerMinute = costPerMinute;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public LocationEmbeddable getLocation() {
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

    public void setLocation(LocationEmbeddable location) {
        this.location = location;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public void setCostPerMinute(Double costPerMinute) {
        this.costPerMinute = costPerMinute;
    }
}
