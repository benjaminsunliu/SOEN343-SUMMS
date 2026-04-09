package com.thehorselegend.summs.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "google.maps.api")
public class GoogleMapsProperties {
    private String key;
}
