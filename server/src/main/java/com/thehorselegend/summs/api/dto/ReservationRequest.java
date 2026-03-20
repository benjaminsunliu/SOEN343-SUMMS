package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(
        @NotNull LocationDto startLocation,
        @NotNull LocationDto endLocation,
        String city,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
