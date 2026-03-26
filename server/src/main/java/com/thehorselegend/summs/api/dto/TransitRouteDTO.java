package com.thehorselegend.summs.api.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitRouteDTO {
    private Long         routeId;
    private String       lineNumber;    
    private String       lineName;      
    private String       type;          
    private String       origin;
    private String       destination;
    private String       departureTime; 
    private String       arrivalTime;   
    private Integer      durationMinutes;
    private Integer      transfers;
    private Double       fare;
    private String       status;        
    private String       statusMessage;
    private List<String> stops;
    private String       lineColor;
}
