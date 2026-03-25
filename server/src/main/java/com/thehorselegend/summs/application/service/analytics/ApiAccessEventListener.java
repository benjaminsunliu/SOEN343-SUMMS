package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.domain.analytics.ApiAccessEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Event listener for API access events (Observer Pattern).
 * Listens to ApiAccessEvents published by the interceptor and updates gateway analytics.
 * Decouples API tracking from analytics computation.
 */
@Service
public class ApiAccessEventListener {

    private final GatewayAnalyticsService gatewayAnalyticsService;

    public ApiAccessEventListener(GatewayAnalyticsService gatewayAnalyticsService) {
        this.gatewayAnalyticsService = gatewayAnalyticsService;
    }

    // Handle API access events.
    // Records the access in all relevant time windows.
    @EventListener
    public void onApiAccess(ApiAccessEvent event) {
        String endpoint = mapEndpointName(event.getEndpoint(), event.getMethod());

        // Only track specific managed endpoints (not auth, not health checks, etc.)
        if (shouldTrackEndpoint(endpoint)) {
            // Record in all time windows
            gatewayAnalyticsService.recordApiAccess(endpoint, "24H");
            gatewayAnalyticsService.recordApiAccess(endpoint, "WEEK");
            gatewayAnalyticsService.recordApiAccess(endpoint, "MONTH");
        }
    }

    
    // Map raw endpoint path to a friendly analytics name. 
    private String mapEndpointName(String endpoint, String method) {
        if (endpoint.contains("/reservations") && method.equals("POST")) {
            return "VEHICLE_RESERVATION";
        } else if (endpoint.contains("/locations") && method.equals("GET")) {
            return "GET_TRANSIT_DETAILS";
        } else if (endpoint.contains("/vehicles") && method.equals("GET")) {
            return "VEHICLE_SEARCH";
        } else if (endpoint.contains("/vehicles") && (method.equals("POST") || method.equals("PUT"))) {
            return "VEHICLE_MANAGEMENT";
        }
        // Add more mappings as needed
        return "OTHER";
    }

    
    // Determine whether to track this endpoint.
    // Filter out auth endpoints, health checks, etc.
    private boolean shouldTrackEndpoint(String endpoint) {
        // Don't track auth and admin analytics endpoints
        if (endpoint.contains("/auth") || endpoint.contains("analytics")) {
            return false;
        }
        return !endpoint.equals("OTHER");
    }
}
