package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.api.dto.RentalAnalyticsResponseDto;
import com.thehorselegend.summs.application.mapper.RentalAnalyticsMetricMapper;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.VehicleReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.LocationEmbeddable;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RentalAnalyticsService.
 * Tests the query-based analytics computation logic.
 */
@ExtendWith(MockitoExtension.class)
class RentalAnalyticsServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RentalAnalyticsMetricRepository analyticsRepository;

    @Mock
    private RentalAnalyticsMetricMapper analyticsMapper;

    private RentalAnalyticsService rentalAnalyticsService;

    @BeforeEach
    void setUp() {
        rentalAnalyticsService = new RentalAnalyticsService(
                reservationRepository,
                vehicleRepository,
                analyticsRepository,
                analyticsMapper
        );
    }

    @Test
    void testComputeAndUpdateMetrics_WithActiveReservations() {
        // Arrange
        ReservationEntity reservation1 = createMockReservation(1L, 100L, ReservationStatus.ACTIVE);
        ReservationEntity reservation2 = createMockReservation(2L, 101L, ReservationStatus.ACTIVE);
        ReservationEntity reservation3 = createMockReservation(3L, 100L, ReservationStatus.ACTIVE);

        List<ReservationEntity> activeReservations = Arrays.asList(reservation1, reservation2, reservation3);

        CarEntity car = new CarEntity();
        car.setId(100L);

        BicycleEntity bicycle = new BicycleEntity();
        bicycle.setId(101L);

        List<VehicleEntity> allVehicles = Arrays.asList(car, bicycle);

        when(reservationRepository.findByStatus(ReservationStatus.ACTIVE))
                .thenReturn(activeReservations);
        when(vehicleRepository.findAll())
                .thenReturn(allVehicles);
        when(analyticsRepository.findByMetricNameAndDimension(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(analyticsRepository.save(any(RentalAnalyticsMetricEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        rentalAnalyticsService.computeAndUpdateMetrics();

        // Assert
        // Verify that total metric was saved
        verify(analyticsRepository, atLeastOnce()).save(argThat(metric ->
                metric.getMetricName().equals("ACTIVE_RENTALS") &&
                metric.getDimension().equals("TOTAL") &&
                metric.getCount() == 3L
        ));

        // Verify vehicle type metrics were saved
        verify(analyticsRepository, atLeastOnce()).save(argThat(metric ->
                metric.getMetricName().equals("ACTIVE_RENTALS") &&
                metric.getDimension().equals("CAR") &&
                metric.getCount() == 2L
        ));

        verify(analyticsRepository, atLeastOnce()).save(argThat(metric ->
                metric.getMetricName().equals("ACTIVE_RENTALS") &&
                metric.getDimension().equals("BICYCLE") &&
                metric.getCount() == 1L
        ));
    }

    @Test
    void testComputeAndUpdateMetrics_WithNoActiveReservations() {
        // Arrange
        when(reservationRepository.findByStatus(ReservationStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(vehicleRepository.findAll())
                .thenReturn(Collections.emptyList());
        when(analyticsRepository.findByMetricNameAndDimension(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(analyticsRepository.save(any(RentalAnalyticsMetricEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        rentalAnalyticsService.computeAndUpdateMetrics();

        // Assert
        verify(analyticsRepository).save(argThat(metric ->
                metric.getMetricName().equals("ACTIVE_RENTALS") &&
                metric.getDimension().equals("TOTAL") &&
                metric.getCount() == 0L
        ));
    }

    @Test
    void testGetAnalytics_ReturnsCorrectDTO() {
        // Arrange - this test verifies the DTO structure
        // Since we mock findByStatus to return empty, services will compute 0 actives
        RentalAnalyticsMetricEntity totalMetric = new RentalAnalyticsMetricEntity(
                "ACTIVE_RENTALS", "TOTAL", 0L  // Matches computed value of 0
        );

        when(reservationRepository.findByStatus(ReservationStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(vehicleRepository.findAll())
                .thenReturn(Collections.emptyList());
        when(analyticsRepository.findByMetricNameAndDimension("ACTIVE_RENTALS", "TOTAL"))
                .thenReturn(Optional.of(totalMetric));
        when(analyticsRepository.findByMetricName("ACTIVE_RENTALS"))
                .thenReturn(Arrays.asList(totalMetric));
        when(analyticsRepository.save(any(RentalAnalyticsMetricEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RentalAnalyticsResponseDto response = rentalAnalyticsService.getAnalytics();

        // Assert - verify DTO structure and values
        assertNotNull(response);
        assertEquals(0L, response.getTotalActiveRentals());  // Computed from empty list
        assertNotNull(response.getRentalsByVehicleType());
        assertTrue(response.getRentalsByVehicleType().isEmpty());
    }

    // Helper method
    private ReservationEntity createMockReservation(Long id, Long vehicleId, ReservationStatus status) {
        VehicleReservationEntity reservation = new VehicleReservationEntity();
        reservation.setId(id);
        reservation.setReservableId(vehicleId);
        reservation.setStatus(status);
        reservation.setUserId(999L);
        reservation.setStartDate(LocalDateTime.now());
        reservation.setEndDate(LocalDateTime.now().plusHours(1));
        reservation.setCity("Montreal");
        reservation.setStartLocation(new LocationEmbeddable(45.5017, -73.5673));
        reservation.setEndLocation(new LocationEmbeddable(45.5017, -73.5673));
        return reservation;
    }
}
