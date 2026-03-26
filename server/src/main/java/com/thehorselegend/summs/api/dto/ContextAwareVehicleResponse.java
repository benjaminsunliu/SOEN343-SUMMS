package com.thehorselegend.summs.api.dto;

/**
 * Vehicle response enriched with weather risk metadata for context-aware search.
 */
public record ContextAwareVehicleResponse(
        Long id,
        String type,            // BICYCLE, SCOOTER, or CAR
        String status,          // AVAILABLE, RESERVED, IN_USE, UNAVAILABLE
        LocationDto location,
        String locationAddress,
        String locationCity,
        Long providerId,
        Double costPerMinute,

        // Scooter-specific
        Double maxRange,

        // Car-specific
        String licensePlate,
        Integer seatingCapacity,

        // Context-aware weather warning fields
        Boolean weatherRisky,
        String weatherRiskMessage
) {
}
