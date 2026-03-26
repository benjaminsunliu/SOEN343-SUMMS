package com.thehorselegend.summs.api.dto;

import lombok.Data;

@Data
public class TransitSearchRequestDTO {
    private String origin;
    private String destination;
    private String date;       
    private String time;       
    private String type; 
}
