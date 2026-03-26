package com.thehorselegend.summs.api.dto;

// DTO representing a single API access metric.
// Used in API responses for gateway-level analytics.
public class ApiAccessMetricDto {

    private String endpoint;
    private String timeWindow;
    private Long accessCount;

    public ApiAccessMetricDto() {
    }

    public ApiAccessMetricDto(String endpoint, String timeWindow, Long accessCount) {
        this.endpoint = endpoint;
        this.timeWindow = timeWindow;
        this.accessCount = accessCount;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(String timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }
}
