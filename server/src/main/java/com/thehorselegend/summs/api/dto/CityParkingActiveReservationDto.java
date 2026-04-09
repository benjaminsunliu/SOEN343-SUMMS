package com.thehorselegend.summs.api.dto;

public record CityParkingActiveReservationDto(
        Long reservationId,
        Long facilityId,
        String facilityName,
        String city,
        String arrivalDate,
        String arrivalTime,
        Integer durationHours,
        Double totalCost,
        String status,
        String confirmedAt
) {
}
