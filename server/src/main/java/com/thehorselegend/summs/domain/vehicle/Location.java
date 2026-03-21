package com.thehorselegend.summs.domain.vehicle;

/**
 * Immutable value object representing a geographic location.
 * Encapsulates latitude and longitude as a cohesive unit.
 * Latitude range: -90 to 90
 * Longitude range: -180 to 180
 */
public record Location(Double latitude, Double longitude) {
    public Location {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude cannot be null");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}
