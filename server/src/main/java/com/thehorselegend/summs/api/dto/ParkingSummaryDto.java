package com.thehorselegend.summs.api.dto;

public record ParkingSummaryDto(
        int totalFacilities,
        int totalSpots,
        int availableSpots,
        int reservedSpots
) {
}
