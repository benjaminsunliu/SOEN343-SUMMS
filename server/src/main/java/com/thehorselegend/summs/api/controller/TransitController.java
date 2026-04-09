package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import com.thehorselegend.summs.application.service.transit.TransitSearchService;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogRepository;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transit")
@RequiredArgsConstructor
public class TransitController {
    private final TransitSearchService transitSearchService;
    private final TransitSearchLogRepository searchLogRepository;
    private final TransitSearchResultRepository searchResultRepository;

    /**
     * GET /api/transit/routes
     * Search for transit routes matching the given criteria.
     */
    @GetMapping("/routes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransitRouteDTO>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "ALL") String type) {

        TransitSearchRequestDTO request = new TransitSearchRequestDTO();
        request.setOrigin(origin);
        request.setDestination(destination);
        request.setDate(date);
        request.setTime(time);
        request.setType(type);

        return ResponseEntity.ok(transitSearchService.searchRoutes(request));
    }

    /**
     * GET /api/transit/status
     * Returns live status for all monitored lines.
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransitLineStatusDTO>> getLineStatuses() {
        return ResponseEntity.ok(transitSearchService.getLineStatuses());
    }

    /**
     * GET /api/transit/analytics
     * Returns analytics data for transit searches.
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('CITY_PROVIDER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransitAnalytics() {
        Map<String, Object> analytics = new LinkedHashMap<>();

        analytics.put("totalSearches",    searchLogRepository.count());
        analytics.put("topOrigins",       searchLogRepository.findTopOrigins().stream()
            .limit(5)
            .map(r -> Map.of("origin", r[0], "count", r[1]))
            .collect(java.util.stream.Collectors.toList()));
        analytics.put("topDestinations",  searchLogRepository.findTopDestinations().stream()
            .limit(5)
            .map(r -> Map.of("destination", r[0], "count", r[1]))
            .collect(java.util.stream.Collectors.toList()));
        analytics.put("searchesByType",   searchLogRepository.findSearchesByType().stream()
            .map(r -> Map.of("type", r[0], "count", r[1]))
            .collect(java.util.stream.Collectors.toList()));

        analytics.put("topResultTransitTypes", searchResultRepository.findTopResultTransitTypes().stream()
            .limit(5)
            .map(r -> Map.of("type", r[0], "count", r[1]))
            .collect(java.util.stream.Collectors.toList()));

        analytics.put("topResultLines", searchResultRepository.findTopResultLines().stream()
            .limit(10)
            .map(r -> Map.of("lineNumber", r[0], "lineName", r[1], "count", r[2]))
            .collect(java.util.stream.Collectors.toList()));

        return ResponseEntity.ok(analytics);
    }
}
