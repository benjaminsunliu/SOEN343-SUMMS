package com.thehorselegend.summs.api.dto;

import java.util.List;

/**
 * Context-aware vehicle search payload containing weather context and vehicles.
 */
public record ContextAwareVehicleSearchResponse(
        String weatherType,
        String weatherSeverity,
        String weatherAdvisory,
        List<ContextAwareVehicleResponse> vehicles
) {
}
