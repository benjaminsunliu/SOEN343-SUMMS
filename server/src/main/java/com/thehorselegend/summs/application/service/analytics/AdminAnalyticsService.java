package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto;
import com.thehorselegend.summs.api.dto.RentalAnalyticsResponseDto;
import org.springframework.stereotype.Service;

// Facade service for all analytics operations (Facade Pattern).
// Provides a single entry point for retrieving both rental and gateway analytics.
@Service
public class AdminAnalyticsService {

    private final RentalAnalyticsService rentalAnalyticsService;
    private final GatewayAnalyticsService gatewayAnalyticsService;

    public AdminAnalyticsService(
            RentalAnalyticsService rentalAnalyticsService,
            GatewayAnalyticsService gatewayAnalyticsService) {
        this.rentalAnalyticsService = rentalAnalyticsService;
        this.gatewayAnalyticsService = gatewayAnalyticsService;
    }

    /**
     * Retrieve all rental-service analytics.
     * Includes total active rentals and breakdown by vehicle type.
     */
    public RentalAnalyticsResponseDto getRentalAnalytics() {
        return rentalAnalyticsService.getAnalytics();
    }

    /**
     * Retrieve all gateway-level analytics.
     * Includes API access counts per endpoint for different time windows.
     */
    public GatewayAnalyticsResponseDto getGatewayAnalytics() {
        return gatewayAnalyticsService.getAnalytics();
    }
}
