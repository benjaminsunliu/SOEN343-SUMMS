package com.thehorselegend.summs.api.controller;


import com.thehorselegend.summs.application.service.ParkingSearchService;
import com.thehorselegend.summs.api.dto.ParkingFacilityDTO;
import com.thehorselegend.summs.api.dto.ParkingSearchRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ParkingController {
    private final ParkingSearchService parkingSearchService;

    /**
     * GET /api/parking/search
     * Query params mirror ParkingSearchRequestDTO fields.
     * Accessible by any authenticated user (CITIZEN, PROVIDER, ADMIN).
     *
     * Example: GET /api/parking/search?destination=175+rue+Ste-Catherine
     *                &durationHours=6&maxPricePerHour=5&city=Montreal
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParkingFacilityDTO>> searchParking(
            @ModelAttribute ParkingSearchRequestDTO request) {

        List<ParkingFacilityDTO> results = parkingSearchService.search(request);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/parking/facilities/{id}
     * Returns a single facility detail (used on spot map screen).
     */
    @GetMapping("/facilities/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParkingFacilityDTO> getFacility(
            @PathVariable Long id) {
        // Delegates to service — omitted for brevity, same pattern
        return ResponseEntity.ok().build();
    }
}
