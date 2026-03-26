package com.thehorselegend.summs.api.dto;

// DTO representing a single rental analytics metric.
// Used in API responses for rental-service analytics.
public class RentalAnalyticsMetricDto {

    private String metricName;
    private String dimension;
    private Long count;

    public RentalAnalyticsMetricDto() {
    }

    public RentalAnalyticsMetricDto(String metricName, String dimension, Long count) {
        this.metricName = metricName;
        this.dimension = dimension;
        this.count = count;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
