package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.RentalAnalyticsResponseDto;
import com.thehorselegend.summs.application.service.analytics.AdminAnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AnalyticsController.
 * Tests controller delegation to service layer.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AdminAnalyticsService adminAnalyticsService;

    @Test
    void testGetRentalAnalytics_ReturnsResponseFromService() {
        // Arrange
        AnalyticsController controller = new AnalyticsController(adminAnalyticsService);
        RentalAnalyticsResponseDto mockResponse = new RentalAnalyticsResponseDto(42L, new ArrayList<>());
        when(adminAnalyticsService.getRentalAnalytics()).thenReturn(mockResponse);

        // Act
        RentalAnalyticsResponseDto result = controller.getRentalAnalytics();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTotalActiveRentals());
    }

    @Test
    void testGetGatewayAnalytics_ReturnsResponseFromService() {
        // Arrange
        AnalyticsController controller = new AnalyticsController(adminAnalyticsService);
        java.util.Map<String, java.util.List<com.thehorselegend.summs.api.dto.ApiAccessMetricDto>> metricsMap = new java.util.LinkedHashMap<>();
        metricsMap.put("TWENTY_FOUR_HOURS", new ArrayList<>());
        metricsMap.put("WEEK", new ArrayList<>());
        metricsMap.put("MONTH", new ArrayList<>());
        com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto mockResponse =
                new com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto(metricsMap);
        when(adminAnalyticsService.getGatewayAnalytics()).thenReturn(mockResponse);

        // Act
        com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto result = controller.getGatewayAnalytics();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMetrics());
    }
}
