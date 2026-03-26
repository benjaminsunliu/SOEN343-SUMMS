package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for storing gateway-level analytics snapshots.
 * Tracks the number of API requests to various endpoints/services.
 */
@Entity
@Table(name = "api_access_metrics", indexes = {
        @Index(name = "idx_endpoint", columnList = "endpoint"),
        @Index(name = "idx_time_window", columnList = "timeWindow")
})
public class ApiAccessMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String endpoint;  // e.g., "VEHICLE_RESERVATION", "GET_TRANSIT_DETAILS", "PARKING_RESERVATION"

    @Column(nullable = false, length = 50)
    private String timeWindow;  // e.g., "24H", "WEEK", "MONTH"

    @Column(nullable = false)
    private Long accessCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

    public ApiAccessMetricEntity() {
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public ApiAccessMetricEntity(String endpoint, String timeWindow, Long accessCount) {
        this();
        this.endpoint = endpoint;
        this.timeWindow = timeWindow;
        this.accessCount = accessCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
