package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing rental analytics metrics from the database.
 * Provides specialized queries for rental analytics retrieval.
 */
@Repository
public interface RentalAnalyticsMetricRepository extends JpaRepository<RentalAnalyticsMetricEntity, Long> {

    /**
     * Find a rental metric by name and dimension.
     * @param metricName the name of the metric (e.g., "ACTIVE_RENTALS")
     * @param dimension the dimension (e.g., "TOTAL" or vehicle type)
     * @return the metric if found
     */
    Optional<RentalAnalyticsMetricEntity> findByMetricNameAndDimension(String metricName, String dimension);

    /**
     * Find all metrics of a specific type.
     * @param metricName the name of the metric
     * @return list of all metrics with this name
     */
    List<RentalAnalyticsMetricEntity> findByMetricName(String metricName);

    /**
     * Find all active rental metrics (all dimensions).
     * @return list of all active rental metrics
     */
    @Query("SELECT r FROM RentalAnalyticsMetricEntity r WHERE r.metricName = 'ACTIVE_RENTALS'")
    List<RentalAnalyticsMetricEntity> findAllActiveRentalMetrics();
}
