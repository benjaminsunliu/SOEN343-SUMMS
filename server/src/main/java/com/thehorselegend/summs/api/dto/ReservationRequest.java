package com.thehorselegend.summs.api.dto;

import java.time.LocalDateTime;

public record ReservationRequest(
        String startLocation,
        String endLocation,
        String city,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
