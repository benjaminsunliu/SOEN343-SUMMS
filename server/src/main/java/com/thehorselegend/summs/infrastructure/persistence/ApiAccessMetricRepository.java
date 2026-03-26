package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing gateway-level analytics metrics from the database.
 * Provides specialized queries for API access analytics retrieval.
 */
@Repository
public interface ApiAccessMetricRepository extends JpaRepository<ApiAccessMetricEntity, Long> {

    /**
     * Find a metric by endpoint and time window.
     * @param endpoint the API endpoint name (e.g., "VEHICLE_RESERVATION")
     * @param timeWindow the time window (e.g., "24H", "WEEK", "MONTH")
     * @return the metric if found
     */
    Optional<ApiAccessMetricEntity> findByEndpointAndTimeWindow(String endpoint, String timeWindow);

    /**
     * Find all metrics for a specific time window.
     * @param timeWindow the time window (e.g., "24H", "WEEK", "MONTH")
     * @return list of metrics for this time window
     */
    List<ApiAccessMetricEntity> findByTimeWindow(String timeWindow);

    /**
     * Find all metrics for a specific endpoint across all time windows.
     * @param endpoint the API endpoint name
     * @return list of metrics for this endpoint
     */
    List<ApiAccessMetricEntity> findByEndpoint(String endpoint);
}
