package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.api.dto.RentalAnalyticsMetricDto;
import com.thehorselegend.summs.api.dto.RentalAnalyticsResponseDto;
import com.thehorselegend.summs.application.mapper.RentalAnalyticsMetricMapper;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Service for rental-service analytics using query-based strategy (Strategy Pattern).
// Computes rental metrics by querying the Reservation table directly.
@Service
public class RentalAnalyticsService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalAnalyticsMetricRepository analyticsRepository;
    private final RentalAnalyticsMetricMapper analyticsMapper;

    public RentalAnalyticsService(
            ReservationRepository reservationRepository,
            VehicleRepository vehicleRepository,
            RentalAnalyticsMetricRepository analyticsRepository,
            RentalAnalyticsMetricMapper analyticsMapper) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
        this.analyticsRepository = analyticsRepository;
        this.analyticsMapper = analyticsMapper;
    }

    /**
     * Compute and update all rental analytics metrics.
     * Queries the Reservation table for active rentals and rental counts by vehicle type.
     */
    public void computeAndUpdateMetrics() {
        // Compute total active rentals
        List<ReservationEntity> activeReservations = reservationRepository.findByStatus(ReservationStatus.ACTIVE);
        Long totalActiveCount = (long) activeReservations.size();

        // Update total active rentals metric
        updateMetric("ACTIVE_RENTALS", "TOTAL", totalActiveCount);

        // Compute rentals by vehicle type (discriminator column in vehicle_type on vehicles table)
        Map<String, Long> rentalsByType = computeRentalsByVehicleType(activeReservations);
        rentalsByType.forEach((vehicleType, count) ->
                updateMetric("ACTIVE_RENTALS", vehicleType, count)
        );
    }

    /**
     * Retrieve current rental analytics snapshot.
     * Returns the latest computed metrics.
     */
    public RentalAnalyticsResponseDto getAnalytics() {
        // Recompute fresh metrics
        computeAndUpdateMetrics();

        // Retrieve metrics from database
        RentalAnalyticsMetricEntity totalMetric = analyticsRepository
                .findByMetricNameAndDimension("ACTIVE_RENTALS", "TOTAL")
                .orElse(new RentalAnalyticsMetricEntity("ACTIVE_RENTALS", "TOTAL", 0L));

        List<RentalAnalyticsMetricEntity> typeMetrics = analyticsRepository
                .findByMetricName("ACTIVE_RENTALS")
                .stream()
                .filter(m -> !m.getDimension().equals("TOTAL"))
                .collect(Collectors.toList());

        List<RentalAnalyticsMetricDto> typeMetricDtos = typeMetrics.stream()
                .map(analyticsMapper::entityToDto)
                .collect(Collectors.toList());

        return new RentalAnalyticsResponseDto(
                totalMetric.getCount(),
                typeMetricDtos
        );
    }

    /**
     * Helper method to update a metric in the database.
     * Creates or updates the metric as needed.
     */
    private void updateMetric(String metricName, String dimension, Long count) {
        RentalAnalyticsMetricEntity metric = analyticsRepository
                .findByMetricNameAndDimension(metricName, dimension)
                .orElse(new RentalAnalyticsMetricEntity(metricName, dimension, count));

        metric.setCount(count);
        analyticsRepository.save(metric);
    }

    /**
     * Helper method to compute rentals grouped by vehicle type.
     * Gets the class name of each vehicle to determine its type.
     */
    private Map<String, Long> computeRentalsByVehicleType(List<ReservationEntity> activeReservations) {
        // Build a map of vehicle ID to vehicle type
        List<VehicleEntity> allVehicles = vehicleRepository.findAll();
        Map<Long, String> vehicleIdToType = allVehicles.stream()
                .collect(Collectors.toMap(
                        VehicleEntity::getId,
                        vehicle -> {
                            // Get the actual class name (CAR, BICYCLE, SCOOTER)
                            String className = vehicle.getClass().getSimpleName();
                            return className.replace("Entity", "").toUpperCase();
                        }
                ));

        // Group reservations by vehicle type
        return activeReservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> vehicleIdToType.getOrDefault(
                                reservation.getReservableId(),
                                "UNKNOWN"
                        ),
                        Collectors.counting()
                ));
    }
}
