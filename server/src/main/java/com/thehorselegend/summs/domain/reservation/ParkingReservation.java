package com.thehorselegend.summs.domain.reservation;

import java.time.LocalDateTime;

public class ParkingReservation extends Reservation {

    private String facilityName;
    private String facilityAddress;
    private Integer durationHours;
    private Double totalCost;

    public ParkingReservation(
            Long reservationId,
            Long userId,
            Long facilityId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String city,
            ReservationStatus status,
            String facilityName,
            String facilityAddress,
            Integer durationHours,
            Double totalCost
    ) {
        super(reservationId, userId, facilityId, startDate, endDate, city, status);
        this.facilityName = facilityName;
        this.facilityAddress = facilityAddress;
        this.durationHours = durationHours;
        this.totalCost = totalCost;
    }

    public ParkingReservation(
            Long userId,
            Long facilityId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String city,
            String facilityName,
            String facilityAddress,
            Integer durationHours,
            Double totalCost
    ) {
        super(userId, facilityId, startDate, endDate, city, ReservationStatus.PENDING);
        this.facilityName = facilityName;
        this.facilityAddress = facilityAddress;
        this.durationHours = durationHours;
        this.totalCost = totalCost;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityAddress() {
        return facilityAddress;
    }

    public void setFacilityAddress(String facilityAddress) {
        this.facilityAddress = facilityAddress;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}
