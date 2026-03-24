package com.thehorselegend.summs.api.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingReservationResponse {
    private Long   reservationId;
    private String facilityName;
    private String facilityAddress;
    private String arrivalDate;
    private String arrivalTime;
    private Integer durationHours;
    private Double totalCost;
    private String status;
    private String confirmedAt;
}
