package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto;
import com.thehorselegend.summs.api.dto.RentalAnalyticsResponseDto;
import com.thehorselegend.summs.application.service.analytics.AdminAnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for analytics operations.
 * Provides endpoints for both rental-service and gateway-level analytics.
 * 
 * All endpoints require ADMIN role access.
 * Follows the same structure as other controllers: receives requests, delegates to service, returns DTOs.
 */
@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    /**
     * GET /api/admin/analytics/rentals
     * Retrieve rental-service analytics.
     * 
     * Returns:
     * - Total number of active rentals
     * - Breakdown of active rentals by vehicle type
     */
    @GetMapping("/rentals")
    @PreAuthorize("hasRole('ADMIN')")
    public RentalAnalyticsResponseDto getRentalAnalytics() {
        return adminAnalyticsService.getRentalAnalytics();
    }

    /**
     * GET /api/admin/analytics/gateway
     * Retrieve gateway-level analytics.
     * 
     * Returns:
     * - Number of API requests to various services (endpoints)
     * - Metrics for past 24 hours, past week, and past month
     */
    @GetMapping("/gateway")
    @PreAuthorize("hasRole('ADMIN')")
    public GatewayAnalyticsResponseDto getGatewayAnalytics() {
        return adminAnalyticsService.getGatewayAnalytics();
    }
}
