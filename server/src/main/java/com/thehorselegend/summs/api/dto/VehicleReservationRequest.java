package com.thehorselegend.summs.api.dto;

import java.time.LocalDateTime;

public class VehicleReservationRequest {

    private LocationDto startLocation;
    private LocationDto endLocation;
    private String city;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public VehicleReservationRequest() {}

    public VehicleReservationRequest(LocationDto startLocation,
                                     LocationDto endLocation,
                                     String city,
                                     LocalDateTime startDate,
                                     LocalDateTime endDate) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocationDto getStartLocation() { return startLocation; }
    public LocationDto getEndLocation() { return endLocation; }
    public String getCity() { return city; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }

    public void setStartLocation(LocationDto startLocation) { this.startLocation = startLocation; }
    public void setEndLocation(LocationDto endLocation) { this.endLocation = endLocation; }
    public void setCity(String city) { this.city = city; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}