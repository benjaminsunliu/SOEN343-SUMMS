package com.thehorselegend.summs.application.service.transit;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogEntity;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogRepository;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchResultEntity;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransitSearchService {
    private final TransitService transitService;
    private final TransitSearchLogRepository searchLogRepository;
    private final TransitSearchResultRepository searchResultRepository;

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

            if (!results.isEmpty()) {
                List<TransitSearchResultEntity> resultEntities = results.stream()
                        .map(route -> TransitSearchResultEntity.builder()
                                .transitType(normalizeValue(route.getType()))
                                .lineNumber(normalizeValue(route.getLineNumber()))
                                .lineName(normalizeValue(route.getLineName()))
                                .build())
                        .toList();
                searchResultRepository.saveAll(resultEntities);
            }

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

    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        return normalized.toUpperCase(Locale.ROOT);
    }
}
