package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

/**
 * JPA Embeddable representation of a geographic location.
 * This allows Location to be stored as columns within VehicleEntity.
 * Maps to the domain Location record.
 */
@Embeddable
public class LocationEmbeddable {

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    public LocationEmbeddable() {
    }

    public LocationEmbeddable(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
