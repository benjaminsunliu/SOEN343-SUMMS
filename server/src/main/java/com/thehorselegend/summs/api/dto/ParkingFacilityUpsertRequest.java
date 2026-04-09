package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingFacilityUpsertRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double pricePerHour;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double rating;

    @NotNull
    @Min(1)
    @Max(100000)
    private Integer totalSpots;

    @NotNull
    private Boolean covered;

    @NotNull
    private Boolean openTwentyFourHours;

    @NotNull
    private Boolean evCharging;

    @NotNull
    private Boolean security;
}
