package com.thehorselegend.summs.api.dto;

import java.util.List;

public record CityParkingAnalyticsResponse(
        int totalFacilities,
        int totalSpots,
        int reservedSpaces,
        int occupiedSpaces,
        int availableSpots,
        double totalRevenue,
        List<CityParkingFacilityAnalyticsDto> facilities,
        List<CityParkingActiveReservationDto> activeReservations
) {
}
