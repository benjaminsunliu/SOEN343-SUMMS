package com.thehorselegend.summs.api.dto;

import java.util.List;

// DTO for gateway-level analytics response.
// Contains API access metrics for various time windows.
public class GatewayAnalyticsResponseDto {

    private List<ApiAccessMetricDto> metricsForPast24Hours;
    private List<ApiAccessMetricDto> metricsForPastWeek;
    private List<ApiAccessMetricDto> metricsForPastMonth;

    public GatewayAnalyticsResponseDto() {
    }

    public GatewayAnalyticsResponseDto(
            List<ApiAccessMetricDto> metricsForPast24Hours,
            List<ApiAccessMetricDto> metricsForPastWeek,
            List<ApiAccessMetricDto> metricsForPastMonth) {
        this.metricsForPast24Hours = metricsForPast24Hours;
        this.metricsForPastWeek = metricsForPastWeek;
        this.metricsForPastMonth = metricsForPastMonth;
    }

    public List<ApiAccessMetricDto> getMetricsForPast24Hours() {
        return metricsForPast24Hours;
    }

    public void setMetricsForPast24Hours(List<ApiAccessMetricDto> metricsForPast24Hours) {
        this.metricsForPast24Hours = metricsForPast24Hours;
    }

    public List<ApiAccessMetricDto> getMetricsForPastWeek() {
        return metricsForPastWeek;
    }

    public void setMetricsForPastWeek(List<ApiAccessMetricDto> metricsForPastWeek) {
        this.metricsForPastWeek = metricsForPastWeek;
    }

    public List<ApiAccessMetricDto> getMetricsForPastMonth() {
        return metricsForPastMonth;
    }

    public void setMetricsForPastMonth(List<ApiAccessMetricDto> metricsForPastMonth) {
        this.metricsForPastMonth = metricsForPastMonth;
    }
}
