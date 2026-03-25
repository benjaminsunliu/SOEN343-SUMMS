package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StartTripRequest(
        @NotNull(message = "Reservation ID is required")
        @Positive(message = "Reservation ID must be greater than 0")
        Long reservationId,

        @NotBlank(message = "Payment authorization code is required")
        String paymentAuthorizationCode
) {
}
