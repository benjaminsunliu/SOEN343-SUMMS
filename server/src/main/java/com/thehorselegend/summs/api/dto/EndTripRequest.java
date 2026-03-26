package com.thehorselegend.summs.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EndTripRequest(
        @Valid
        @NotNull(message = "Drop-off location is required")
        LocationDto dropOffLocation
) {
}
