package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.api.dto.ApiAccessMetricDto;
import com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto;
import com.thehorselegend.summs.application.mapper.ApiAccessMetricMapper;
import com.thehorselegend.summs.infrastructure.persistence.ApiAccessMetricEntity;
import com.thehorselegend.summs.infrastructure.persistence.ApiAccessMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GatewayAnalyticsService.
 * Tests API access tracking and metric storage logic.
 */
@ExtendWith(MockitoExtension.class)
class GatewayAnalyticsServiceTest {

    @Mock
    private ApiAccessMetricRepository apiAccessRepository;

    @Mock
    private ApiAccessMetricMapper apiAccessMapper;

    private GatewayAnalyticsService gatewayAnalyticsService;

    @BeforeEach
    void setUp() {
        gatewayAnalyticsService = new GatewayAnalyticsService(
                apiAccessRepository,
                apiAccessMapper
        );
    }

    @Test
    void testRecordApiAccess_CreatesNewMetric() {
        // Arrange
        String endpoint = "VEHICLE_RESERVATION";
        String timeWindow = "24H";

        when(apiAccessRepository.findByEndpointAndTimeWindow(endpoint, timeWindow))
                .thenReturn(Optional.empty());
        when(apiAccessRepository.save(any(ApiAccessMetricEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        gatewayAnalyticsService.recordApiAccess(endpoint, timeWindow);

        // Assert
        verify(apiAccessRepository).save(argThat(metric ->
                metric.getEndpoint().equals(endpoint) &&
                metric.getTimeWindow().equals(timeWindow) &&
                metric.getAccessCount() == 1L
        ));
    }

    @Test
    void testRecordApiAccess_IncrementsExistingMetric() {
        // Arrange
        String endpoint = "VEHICLE_RESERVATION";
        String timeWindow = "24H";

        ApiAccessMetricEntity existingMetric = new ApiAccessMetricEntity(endpoint, timeWindow, 10L);

        when(apiAccessRepository.findByEndpointAndTimeWindow(endpoint, timeWindow))
                .thenReturn(Optional.of(existingMetric));
        when(apiAccessRepository.save(any(ApiAccessMetricEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        gatewayAnalyticsService.recordApiAccess(endpoint, timeWindow);

        // Assert
        assertEquals(11L, existingMetric.getAccessCount());
        verify(apiAccessRepository).save(argThat(metric ->
                metric.getAccessCount() == 11L
        ));
    }

    @Test
    void testGetAnalytics_ReturnsAllTimeWindows() {
        // Arrange
        ApiAccessMetricEntity metric24h = new ApiAccessMetricEntity(
                "VEHICLE_RESERVATION", "24H", 100L
        );
        ApiAccessMetricEntity metricWeek = new ApiAccessMetricEntity(
                "VEHICLE_RESERVATION", "WEEK", 500L
        );
        ApiAccessMetricEntity metricMonth = new ApiAccessMetricEntity(
                "VEHICLE_RESERVATION", "MONTH", 2000L
        );

        when(apiAccessRepository.findByTimeWindow("24H"))
                .thenReturn(Arrays.asList(metric24h));
        when(apiAccessRepository.findByTimeWindow("WEEK"))
                .thenReturn(Arrays.asList(metricWeek));
        when(apiAccessRepository.findByTimeWindow("MONTH"))
                .thenReturn(Arrays.asList(metricMonth));

        ApiAccessMetricDto dto24h = new ApiAccessMetricDto("VEHICLE_RESERVATION", "24H", 100L);
        ApiAccessMetricDto dtoWeek = new ApiAccessMetricDto("VEHICLE_RESERVATION", "WEEK", 500L);
        ApiAccessMetricDto dtoMonth = new ApiAccessMetricDto("VEHICLE_RESERVATION", "MONTH", 2000L);

        when(apiAccessMapper.entityToDto(metric24h)).thenReturn(dto24h);
        when(apiAccessMapper.entityToDto(metricWeek)).thenReturn(dtoWeek);
        when(apiAccessMapper.entityToDto(metricMonth)).thenReturn(dtoMonth);

        // Act
        GatewayAnalyticsResponseDto response = gatewayAnalyticsService.getAnalytics();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getMetrics());
        assertNotNull(response.getMetrics().get("TWENTY_FOUR_HOURS"));
        assertNotNull(response.getMetrics().get("WEEK"));
        assertNotNull(response.getMetrics().get("MONTH"));
        assertEquals(1, response.getMetrics().get("TWENTY_FOUR_HOURS").size());
        assertEquals(1, response.getMetrics().get("WEEK").size());
        assertEquals(1, response.getMetrics().get("MONTH").size());
    }

    @Test
    void testGetAnalytics_ReturnsEmptyListsWhenNoMetrics() {
        // Arrange
        when(apiAccessRepository.findByTimeWindow("24H"))
                .thenReturn(new ArrayList<>());
        when(apiAccessRepository.findByTimeWindow("WEEK"))
                .thenReturn(new ArrayList<>());
        when(apiAccessRepository.findByTimeWindow("MONTH"))
                .thenReturn(new ArrayList<>());

        // Act
        GatewayAnalyticsResponseDto response = gatewayAnalyticsService.getAnalytics();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getMetrics());
        assertTrue(response.getMetrics().get("TWENTY_FOUR_HOURS").isEmpty());
        assertTrue(response.getMetrics().get("WEEK").isEmpty());
        assertTrue(response.getMetrics().get("MONTH").isEmpty());
    }
}
