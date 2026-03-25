package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for storing rental-service analytics snapshots.
 * Represents aggregated rental metrics (e.g., active rentals, rentals per vehicle type).
 */
@Entity
@Table(name = "rental_analytics_metrics")
public class RentalAnalyticsMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String metricName;  // e.g., "ACTIVE_RENTALS", "RENTALS_PER_TYPE"

    @Column(nullable = false, length = 100)
    private String dimension;  // e.g., vehicle type ("CAR", "BICYCLE"), city name, or "TOTAL"

    @Column(nullable = false)
    private Long count;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

    public RentalAnalyticsMetricEntity() {
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public RentalAnalyticsMetricEntity(String metricName, String dimension, Long count) {
        this();
        this.metricName = metricName;
        this.dimension = dimension;
        this.count = count;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.lastModifiedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }
}
