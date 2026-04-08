package com.thehorselegend.summs.api.dto;

public record ParkingCatalogEntryDto(
        String terrainCode,
        String name,
        String address,
        String city,
        double latitude,
        double longitude,
        double pricePerHour,
        double rating,
        int totalSpots,
        boolean covered,
        boolean openTwentyFourHours,
        boolean evCharging,
        boolean security,
        boolean added,
        Long addedFacilityId
) {
}
