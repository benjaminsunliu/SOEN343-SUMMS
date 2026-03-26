package com.thehorselegend.summs.api.dto;

import java.util.List;


// DTO for rental-service analytics response.
// Contains all rental-related metrics.
public class RentalAnalyticsResponseDto {

    private Long totalActiveRentals;
    private List<RentalAnalyticsMetricDto> rentalsByVehicleType;

    public RentalAnalyticsResponseDto() {
    }

    public RentalAnalyticsResponseDto(Long totalActiveRentals, List<RentalAnalyticsMetricDto> rentalsByVehicleType) {
        this.totalActiveRentals = totalActiveRentals;
        this.rentalsByVehicleType = rentalsByVehicleType;
    }

    public Long getTotalActiveRentals() {
        return totalActiveRentals;
    }

    public void setTotalActiveRentals(Long totalActiveRentals) {
        this.totalActiveRentals = totalActiveRentals;
    }

    public List<RentalAnalyticsMetricDto> getRentalsByVehicleType() {
        return rentalsByVehicleType;
    }

    public void setRentalsByVehicleType(List<RentalAnalyticsMetricDto> rentalsByVehicleType) {
        this.rentalsByVehicleType = rentalsByVehicleType;
    }
}
