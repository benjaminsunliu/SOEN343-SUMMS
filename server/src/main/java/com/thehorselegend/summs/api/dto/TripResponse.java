package com.thehorselegend.summs.api.dto;

import java.time.LocalDateTime;

public record TripResponse(
        Long tripId,
        Long reservationId,
        Long vehicleId,
        Long citizenId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long totalDurationMinutes,
        String vehicleStatus,
        Double co2SavedKg
) {
}
