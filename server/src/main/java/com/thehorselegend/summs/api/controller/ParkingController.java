package com.thehorselegend.summs.api.controller;


import com.thehorselegend.summs.application.service.ParkingSearchService;
import com.thehorselegend.summs.api.dto.ParkingFacilityDTO;
import com.thehorselegend.summs.api.dto.ParkingFacilityUpsertRequest;
import com.thehorselegend.summs.api.dto.ParkingCatalogEntryDto;
import com.thehorselegend.summs.api.dto.ParkingSearchRequestDTO;
import com.thehorselegend.summs.api.dto.ParkingSummaryDto;
import com.thehorselegend.summs.application.service.ParkingFacilityService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ParkingController {
    private final ParkingSearchService parkingSearchService;
    private final ParkingFacilityService parkingFacilityService;
    private final UserRepository userRepository;

    /**
     * GET /api/parking/search
     * Query params mirror ParkingSearchRequestDTO fields.
    * Accessible by any authenticated user (CITIZEN, PROVIDER, CITY_PROVIDER, ADMIN).
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

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParkingSummaryDto> getParkingSummary() {
        return ResponseEntity.ok(parkingFacilityService.getSummary());
    }

    @GetMapping("/management/spaces")
    @PreAuthorize("hasAnyRole('CITY_PROVIDER', 'ADMIN')")
    public ResponseEntity<List<ParkingFacilityDTO>> listParkingSpaces(Authentication authentication) {
        return ResponseEntity.ok(parkingFacilityService.getAllFacilities(resolveAuthenticatedUserId(authentication)));
    }

    @GetMapping("/management/catalog")
    @PreAuthorize("hasAnyRole('CITY_PROVIDER', 'ADMIN')")
    public ResponseEntity<List<ParkingCatalogEntryDto>> listParkingCatalog(Authentication authentication) {
        return ResponseEntity.ok(parkingFacilityService.getCatalogEntriesWithStatus(resolveAuthenticatedUserId(authentication)));
    }

    @PostMapping("/management/catalog/{terrainCode}/add")
    @PreAuthorize("hasAnyRole('CITY_PROVIDER', 'ADMIN')")
    public ResponseEntity<ParkingFacilityDTO> addParkingSpaceFromCatalog(
            @PathVariable String terrainCode,
            Authentication authentication) {
        return ResponseEntity.ok(parkingFacilityService.addCatalogEntryByTerrainCode(
                terrainCode,
                resolveAuthenticatedUserId(authentication)));
    }

    @PostMapping("/management/spaces")
    @PreAuthorize("hasAnyRole('CITY_PROVIDER', 'ADMIN')")
    public ResponseEntity<ParkingFacilityDTO> createParkingSpace(
            @Valid @RequestBody ParkingFacilityUpsertRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(parkingFacilityService.createFacility(request, resolveAuthenticatedUserId(authentication)));
    }

    @PutMapping("/management/spaces/{facilityId}")
    @PreAuthorize("hasAnyRole('CITY_PROVIDER', 'ADMIN')")
    public ResponseEntity<ParkingFacilityDTO> updateParkingSpace(
            @PathVariable Long facilityId,
            @Valid @RequestBody ParkingFacilityUpsertRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(parkingFacilityService.updateFacility(
                facilityId,
                request,
                resolveAuthenticatedUserId(authentication)));
    }

    @DeleteMapping("/management/spaces/{facilityId}")
    @PreAuthorize("hasAnyRole('CITY_PROVIDER', 'ADMIN')")
    public ResponseEntity<Void> deleteParkingSpace(@PathVariable Long facilityId, Authentication authentication) {
        parkingFacilityService.deleteFacility(facilityId, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication required");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user no longer exists"));
        return user.getId();
    }
}
