package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto;
import com.thehorselegend.summs.api.dto.RentalAnalyticsResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminAnalyticsService (Facade).
 * Tests that the facade correctly delegates to underlying services.
 */
@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceTest {

    @Mock
    private RentalAnalyticsService rentalAnalyticsService;

    @Mock
    private GatewayAnalyticsService gatewayAnalyticsService;

    @Test
    void testGetRentalAnalytics_DelegatesToRentalService() {
        // Arrange
        AdminAnalyticsService adminService = new AdminAnalyticsService(
                rentalAnalyticsService,
                gatewayAnalyticsService
        );
        RentalAnalyticsResponseDto expectedDto = new RentalAnalyticsResponseDto(42L, null);

        when(rentalAnalyticsService.getAnalytics()).thenReturn(expectedDto);

        // Act
        RentalAnalyticsResponseDto result = adminService.getRentalAnalytics();

        // Assert
        assertNotNull(result);
        verify(rentalAnalyticsService).getAnalytics();
    }

    @Test
    void testGetGatewayAnalytics_DelegatesToGatewayService() {
        // Arrange
        AdminAnalyticsService adminService = new AdminAnalyticsService(
                rentalAnalyticsService,
                gatewayAnalyticsService
        );
        GatewayAnalyticsResponseDto expectedDto = new GatewayAnalyticsResponseDto(null, null, null);

        when(gatewayAnalyticsService.getAnalytics()).thenReturn(expectedDto);

        // Act
        GatewayAnalyticsResponseDto result = adminService.getGatewayAnalytics();

        // Assert
        assertNotNull(result);
        verify(gatewayAnalyticsService).getAnalytics();
    }
}
