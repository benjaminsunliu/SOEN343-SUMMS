package com.thehorselegend.summs.api.dto;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        Long vehicleId,
        Long userId,
        String startLocation,
        String endLocation,
        String city,
        LocalDateTime startDate,
        LocalDateTime endDate,
        ReservationStatus status
) {

}
