package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class VehicleReservationRequest {

    @NotBlank(message = "End address is required")
    private String endAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    public VehicleReservationRequest() {}

    public VehicleReservationRequest(String endAddress,
                                     String city,
                                     LocalDateTime startDate,
                                     LocalDateTime endDate) {
        this.endAddress = endAddress;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getEndAddress() { return endAddress; }
    public String getCity() { return city; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }

    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }
    public void setCity(String city) { this.city = city; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}
