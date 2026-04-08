package com.thehorselegend.summs.application.service.transit;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
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

    public List<TransitRouteDTO> searchRoutes(TransitSearchRequestDTO request) {
        try {
            return transitService.searchRoutes(request);
        } catch (Exception e) {
            log.error("Transit search failed: {}", e.getMessage());
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
