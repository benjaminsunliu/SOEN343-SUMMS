package com.thehorselegend.summs.application.service.analytics;

import com.thehorselegend.summs.api.dto.ApiAccessMetricDto;
import com.thehorselegend.summs.api.dto.GatewayAnalyticsResponseDto;
import com.thehorselegend.summs.application.mapper.ApiAccessMetricMapper;
import com.thehorselegend.summs.infrastructure.persistence.ApiAccessMetricEntity;
import com.thehorselegend.summs.infrastructure.persistence.ApiAccessMetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

 // Service for gateway-level analytics using event-based strategy .
 // Stores and retrieves API access metrics across different time windows.
@Service
public class GatewayAnalyticsService {

    private final ApiAccessMetricRepository apiAccessRepository;
    private final ApiAccessMetricMapper apiAccessMapper;

    public GatewayAnalyticsService(
            ApiAccessMetricRepository apiAccessRepository,
            ApiAccessMetricMapper apiAccessMapper) {
        this.apiAccessRepository = apiAccessRepository;
        this.apiAccessMapper = apiAccessMapper;
    }

    /**
     * Increment access count for a specific endpoint in a time window.
     * @param endpoint the API endpoint identifier
     * @param timeWindow the time window ("24H", "WEEK", "MONTH")
     */
    public void recordApiAccess(String endpoint, String timeWindow) {
        ApiAccessMetricEntity metric = apiAccessRepository
                .findByEndpointAndTimeWindow(endpoint, timeWindow)
                .orElse(new ApiAccessMetricEntity(endpoint, timeWindow, 0L));

        metric.setAccessCount(metric.getAccessCount() + 1);
        apiAccessRepository.save(metric);
    }

    /**
     * Retrieve gateway analytics for all time windows.
     * Returns API call counts per endpoint for 24h, week, and month periods.
     */
    public GatewayAnalyticsResponseDto getAnalytics() {
        List<ApiAccessMetricDto> metrics24h = convertMetrics(apiAccessRepository.findByTimeWindow("24H"));
        List<ApiAccessMetricDto> metricsWeek = convertMetrics(apiAccessRepository.findByTimeWindow("WEEK"));
        List<ApiAccessMetricDto> metricsMonth = convertMetrics(apiAccessRepository.findByTimeWindow("MONTH"));

        return new GatewayAnalyticsResponseDto(metrics24h, metricsWeek, metricsMonth);
    }

    /**
     * Helper method to convert entities to DTOs.
     */
    private List<ApiAccessMetricDto> convertMetrics(List<ApiAccessMetricEntity> entities) {
        return entities.stream()
                .map(apiAccessMapper::entityToDto)
                .collect(Collectors.toList());
    }
}
