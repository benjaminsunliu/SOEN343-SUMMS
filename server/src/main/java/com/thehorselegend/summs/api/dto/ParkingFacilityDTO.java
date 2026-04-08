package com.thehorselegend.summs.api.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingFacilityDTO {
    private Long    facilityId;
    private String  name;
    private String  address;
    private String  city;
    private Double  latitude;
    private Double  longitude;
    private Double  distanceKm;        
    private Double  pricePerHour;
    private Double  estimatedTotal;   
    private Double  rating;
    private Integer availableSpots;
    private Integer totalSpots;
    private String  availabilityStatus; 

    // Amenities
    private Boolean       covered;
    private Boolean       openTwentyFourHours;
    private Boolean       evCharging;
    private Boolean       security;
    private List<String> amenityTags;   
}
