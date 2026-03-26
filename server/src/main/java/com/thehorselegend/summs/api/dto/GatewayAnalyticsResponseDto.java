package com.thehorselegend.summs.api.dto;

import java.util.List;
import java.util.Map;

// DTO for gateway-level analytics response.
// Contains API access metrics organized by time window.
public class GatewayAnalyticsResponseDto {

    private Map<String, List<ApiAccessMetricDto>> metrics;

    public GatewayAnalyticsResponseDto() {
    }

    public GatewayAnalyticsResponseDto(Map<String, List<ApiAccessMetricDto>> metrics) {
        this.metrics = metrics;
    }

    public Map<String, List<ApiAccessMetricDto>> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, List<ApiAccessMetricDto>> metrics) {
        this.metrics = metrics;
    }
}
