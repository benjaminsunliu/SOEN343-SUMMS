package com.thehorselegend.summs.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stm.api")
public class StmApiProperties {
    private String key;
    private String gtfsRtUrl;
    private String alertsUrl;
    private String statusUrl;
}
