package com.thehorselegend.summs.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBicycleRequest(
        @Valid
        @NotNull(message = "Location is required")
        LocationDto location,

        @NotNull(message = "Provider ID is required")
        Long providerId,

        @NotNull(message = "Cost per minute is required")
        @Positive(message = "Cost per minute must be greater than 0")
        Double costPerMinute
) {
}
