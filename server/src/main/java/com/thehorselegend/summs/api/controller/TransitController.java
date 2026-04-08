package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import com.thehorselegend.summs.application.service.transit.TransitSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transit")
@RequiredArgsConstructor
public class TransitController {
    private final TransitSearchService transitSearchService;

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
}
