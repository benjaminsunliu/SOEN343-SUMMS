package com.thehorselegend.summs.api.dto;

import lombok.Data;

@Data
public class CreateParkingReservationRequest {
    private Long    facilityId;
    private String  facilityName;
    private String  facilityAddress;
    private String  city;
    private String  arrivalDate;
    private String  arrivalTime;
    private Integer durationHours;
    private Double  totalCost;
    private String  paymentMethod;
}
