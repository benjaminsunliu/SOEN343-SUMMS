package com.thehorselegend.summs.api.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitLineStatusDTO {
    private String lineId;
    private String lineNumber;
    private String lineName;
    private String type;          
    private String status;        
    private String statusMessage;
    private String lineColor;
}
