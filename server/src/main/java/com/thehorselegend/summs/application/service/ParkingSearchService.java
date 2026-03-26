package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.ParkingFacilityDTO;
import com.thehorselegend.summs.api.dto.ParkingSearchRequestDTO;
import com.thehorselegend.summs.infrastructure.adapter.IParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingSearchService {
    private final IParkingService parkingService;

    public List<ParkingFacilityDTO> search(ParkingSearchRequestDTO request) {
        log.info("Parking search: destination={}, maxPrice={}, duration={}h",
                request.getDestination(),
                request.getMaxPricePerHour(),
                request.getDurationHours());

        List<ParkingFacilityDTO> results;
        try {
            results = parkingService.searchFacilities(request);
        } catch (Exception e) {
            log.error("Parking service unavailable: {}", e.getMessage());
            return List.of();
        }

        log.info("Parking search returned {} facilities", results.size());
        return results;
    }
}
