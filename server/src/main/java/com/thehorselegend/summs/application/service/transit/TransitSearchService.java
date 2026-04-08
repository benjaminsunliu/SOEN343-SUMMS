package com.thehorselegend.summs.application.service.transit;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogEntity;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransitSearchService {
    private final TransitService transitService;
    private final TransitSearchLogRepository searchLogRepository;

    public List<TransitRouteDTO> searchRoutes(TransitSearchRequestDTO request) {
        try {
            List<TransitRouteDTO> results = transitService.searchRoutes(request);

            searchLogRepository.save(TransitSearchLogEntity.builder()
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .transitType(request.getType())
                .date(request.getDate())
                .time(request.getTime())
                .resultsReturned(results.size())
                .build());

            log.info("Transit search log saved successfully");
            return results;

        } catch (Exception e) {
            log.error("Transit search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<TransitLineStatusDTO> getLineStatuses() {
        try {
            return transitService.getLineStatuses();
        } catch (Exception e) {
            log.error("Transit status fetch failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
