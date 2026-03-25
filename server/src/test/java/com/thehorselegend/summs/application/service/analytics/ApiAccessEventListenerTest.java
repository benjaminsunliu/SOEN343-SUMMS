package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.domain.analytics.ApiAccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ApiAccessEventListener.
 * Tests the event mapping and tracking logic.
 */
@ExtendWith(MockitoExtension.class)
class ApiAccessEventListenerTest {

    @Mock
    private GatewayAnalyticsService gatewayAnalyticsService;

    private ApiAccessEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new ApiAccessEventListener(gatewayAnalyticsService);
    }

    @Test
    void testOnApiAccess_VehicleReservationEndpoint() {
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
        // Should record in all 3 time windows for VEHICLE_RESERVATION
        verify(gatewayAnalyticsService).recordApiAccess("VEHICLE_RESERVATION", "24H");
        verify(gatewayAnalyticsService).recordApiAccess("VEHICLE_RESERVATION", "WEEK");
        verify(gatewayAnalyticsService).recordApiAccess("VEHICLE_RESERVATION", "MONTH");
    }

    @Test
    void testOnApiAccess_VehicleSearchEndpoint() {
        // Arrange
        ApiAccessEvent event = new ApiAccessEvent(
                this,
                "/api/vehicles",
                "GET",
                200,
                75L
        );

        // Act
        eventListener.onApiAccess(event);

        // Assert
        verify(gatewayAnalyticsService).recordApiAccess("VEHICLE_SEARCH", "24H");
        verify(gatewayAnalyticsService).recordApiAccess("VEHICLE_SEARCH", "WEEK");
        verify(gatewayAnalyticsService).recordApiAccess("VEHICLE_SEARCH", "MONTH");
    }

    @Test
    void testOnApiAccess_LocationsEndpoint() {
        // Arrange
        ApiAccessEvent event = new ApiAccessEvent(
                this,
                "/api/locations",
                "GET",
                200,
                50L
        );

        // Act
        eventListener.onApiAccess(event);

        // Assert
        verify(gatewayAnalyticsService).recordApiAccess("GET_TRANSIT_DETAILS", "24H");
        verify(gatewayAnalyticsService).recordApiAccess("GET_TRANSIT_DETAILS", "WEEK");
        verify(gatewayAnalyticsService).recordApiAccess("GET_TRANSIT_DETAILS", "MONTH");
    }

    @Test
    void testOnApiAccess_AuthEndpointNotTracked() {
        // Arrange
        ApiAccessEvent event = new ApiAccessEvent(
                this,
                "/api/auth/login",
                "POST",
                200,
                100L
        );

        // Act
        eventListener.onApiAccess(event);

        // Assert
        // Auth endpoints should not be tracked
        verify(gatewayAnalyticsService, never()).recordApiAccess(anyString(), anyString());
    }

    @Test
    void testOnApiAccess_AnalyticsEndpointNotTracked() {
        // Arrange
        ApiAccessEvent event = new ApiAccessEvent(
                this,
                "/api/admin/analytics/gateway",
                "GET",
                200,
                50L
        );

        // Act
        eventListener.onApiAccess(event);

        // Assert
        // Analytics endpoints should not be tracked
        verify(gatewayAnalyticsService, never()).recordApiAccess(anyString(), anyString());
    }
}
