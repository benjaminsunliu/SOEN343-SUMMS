package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record StartTripRequest(
        @NotNull(message = "Vehicle ID is required")
        @Positive(message = "Vehicle ID must be greater than 0")
        Long vehicleId,

        @NotNull(message = "Citizen ID is required")
        @Positive(message = "Citizen ID must be greater than 0")
        Long citizenId,

        @NotBlank(message = "Reservation code is required")
        String reservationCode,

        @NotNull(message = "Reservation validity time is required")
        LocalDateTime reservationValidUntil,

        @NotBlank(message = "Payment authorization code is required")
        String paymentAuthorizationCode
) {
}
