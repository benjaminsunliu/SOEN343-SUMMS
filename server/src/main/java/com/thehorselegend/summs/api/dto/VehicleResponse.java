package com.thehorselegend.summs.api.dto;

/**
 * Polymorphic DTO for vehicle responses.
 * Contains common vehicle fields and type-specific fields (when applicable).
 * Used by all GET endpoints to return vehicle data.
 */
public record VehicleResponse(
        Long id,
        String type,            // BICYCLE, SCOOTER, or CAR
        String status,          // AVAILABLE, RESERVED, IN_USE, UNAVAILABLE
        LocationDto location,
        Long providerId,
        Double costPerMinute,
        
        // Scooter-specific
        Double maxRange,
        
        // Car-specific
        String licensePlate,
        Integer seatingCapacity
) {
}
