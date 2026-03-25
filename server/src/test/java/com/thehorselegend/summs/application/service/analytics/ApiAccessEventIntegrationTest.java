package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.application.mapper.ApiAccessMetricMapper;
import com.thehorselegend.summs.domain.analytics.ApiAccessEvent;
import com.thehorselegend.summs.infrastructure.persistence.ApiAccessMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for event tracking and metric storage.
 * Tests the full flow from event publication to database persistence.
 */
@DataJpaTest
@Import({ApiAccessEventListener.class, GatewayAnalyticsService.class, ApiAccessMetricMapper.class})
class ApiAccessEventIntegrationTest {

    @Autowired
    private ApiAccessMetricRepository apiAccessRepository;

    @Autowired
    private GatewayAnalyticsService gatewayAnalyticsService;

    @Autowired
    private ApiAccessEventListener eventListener;

    @BeforeEach
    void setUp() {
        // Clear any previous test data
        apiAccessRepository.deleteAll();
    }

    @Test
    void testRecordApiAccess_PersistsToDatabase() {
        // Arrange
        String endpoint = "VEHICLE_RESERVATION";
        String timeWindow = "24H";

        // Act
        gatewayAnalyticsService.recordApiAccess(endpoint, timeWindow);

        // Assert
        var metrics = apiAccessRepository.findByEndpointAndTimeWindow(endpoint, timeWindow);
        assertNotNull(metrics);
        assertEquals(1L, metrics.get().getAccessCount());
    }

    @Test
    void testRecordApiAccess_MultipleTimeWindows_AllPersisted() {
        // Arrange
        String endpoint = "GET_TRANSIT_DETAILS";

        // Act
        gatewayAnalyticsService.recordApiAccess(endpoint, "24H");
        gatewayAnalyticsService.recordApiAccess(endpoint, "WEEK");
        gatewayAnalyticsService.recordApiAccess(endpoint, "MONTH");

        // Assert
        assertEquals(1, apiAccessRepository.findByEndpoint(endpoint).size());
        var metric24h = apiAccessRepository.findByEndpointAndTimeWindow(endpoint, "24H");
        var metricWeek = apiAccessRepository.findByEndpointAndTimeWindow(endpoint, "WEEK");
        var metricMonth = apiAccessRepository.findByEndpointAndTimeWindow(endpoint, "MONTH");

        assertEquals(1L, metric24h.get().getAccessCount());
        assertEquals(1L, metricWeek.get().getAccessCount());
        assertEquals(1L, metricMonth.get().getAccessCount());
    }

    @Test
    void testRecordApiAccess_MultipleRecords_Increments() {
        // Arrange
        String endpoint = "VEHICLE_SEARCH";
        String timeWindow = "WEEK";

        // Act
        gatewayAnalyticsService.recordApiAccess(endpoint, timeWindow);
        gatewayAnalyticsService.recordApiAccess(endpoint, timeWindow);
        gatewayAnalyticsService.recordApiAccess(endpoint, timeWindow);

        // Assert
        var metric = apiAccessRepository.findByEndpointAndTimeWindow(endpoint, timeWindow);
        assertEquals(3L, metric.get().getAccessCount());
    }

    @Test
    void testEventListener_MapEndpointCorrectly() {
        // Arrange
        ApiAccessEvent event = new ApiAccessEvent(
                this,
                "/api/reservations",
                "POST",
                201,
                150L
        );

        // Act
        eventListener.onApiAccess(event);

        // Assert
        var metric24h = apiAccessRepository.findByEndpointAndTimeWindow("VEHICLE_RESERVATION", "24H");
        var metricWeek = apiAccessRepository.findByEndpointAndTimeWindow("VEHICLE_RESERVATION", "WEEK");
        var metricMonth = apiAccessRepository.findByEndpointAndTimeWindow("VEHICLE_RESERVATION", "MONTH");

        assertEquals(1L, metric24h.get().getAccessCount());
        assertEquals(1L, metricWeek.get().getAccessCount());
        assertEquals(1L, metricMonth.get().getAccessCount());
    }
}
