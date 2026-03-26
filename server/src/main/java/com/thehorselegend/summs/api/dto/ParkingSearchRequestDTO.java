package com.thehorselegend.summs.api.dto;

import lombok.Data;

@Data
public class ParkingSearchRequestDTO {
    private String destination;      
    private String city;             
    private String arrivalDate;     
    private String arrivalTime;      
    private Integer durationHours;   
    private String vehicleType;      
    private Double maxPricePerHour;  

    private Double latitude;
    private Double longitude;
    private Double radiusKm = 2.0;  
}
