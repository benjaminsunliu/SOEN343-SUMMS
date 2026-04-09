package com.thehorselegend.summs.api.dto;

public record CityParkingFacilityAnalyticsDto(
        Long facilityId,
        String name,
        String city,
        int totalSpots,
        int reservedSpaces,
        int occupiedSpaces,
        int availableSpots,
        double totalRevenue
) {
}
