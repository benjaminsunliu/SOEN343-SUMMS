package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class VehicleReservationRequest {

    @NotNull(message = "End location is required")
    private LocationDto endLocation;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    public VehicleReservationRequest() {}

    public VehicleReservationRequest(LocationDto endLocation,
                                     String city,
                                     LocalDateTime startDate,
                                     LocalDateTime endDate) {
        this.endLocation = endLocation;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocationDto getEndLocation() { return endLocation; }
    public String getCity() { return city; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }

    public void setEndLocation(LocationDto endLocation) { this.endLocation = endLocation; }
    public void setCity(String city) { this.city = city; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}
