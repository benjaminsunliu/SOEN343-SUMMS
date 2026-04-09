package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.application.service.transit.TransitSearchService;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogRepository;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitControllerTest {

    @Mock
    private TransitSearchService transitSearchService;

    @Mock
    private TransitSearchLogRepository searchLogRepository;

    @Mock
    private TransitSearchResultRepository searchResultRepository;

    @Test
    void getTransitAnalytics_usesReturnedTransitTypesFromSearchLogs() {
        TransitController controller = new TransitController(
                transitSearchService,
                searchLogRepository,
                searchResultRepository
        );

        when(searchResultRepository.count()).thenReturn(31L);
        when(searchLogRepository.count()).thenReturn(12L);
        when(searchLogRepository.findTopOrigins()).thenReturn(List.<Object[]>of(new Object[]{"Montreal", 8L}));
        when(searchLogRepository.findTopDestinations()).thenReturn(List.<Object[]>of(new Object[]{"Laval", 6L}));
        when(searchLogRepository.findSearchesByType()).thenReturn(List.<Object[]>of(new Object[]{"ALL", 12L}));
        when(searchLogRepository.findTopReturnedTransitTypes()).thenReturn(List.<Object[]>of(
                new Object[]{"BUS", 9L},
                new Object[]{"METRO", 4L}
        ));
        when(searchResultRepository.findTopResultLines()).thenReturn(List.<Object[]>of(
                new Object[]{"24", "Sherbrooke", 5L}
        ));

        ResponseEntity<Map<String, Object>> response = controller.getTransitAnalytics();

        assertNotNull(response.getBody());
        assertEquals(31L, response.getBody().get("totalTrips"));
        assertEquals(12L, response.getBody().get("totalSearches"));

        Object topResultTransitTypes = response.getBody().get("topResultTransitTypes");
        assertInstanceOf(List.class, topResultTransitTypes);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> typeMetrics = (List<Map<String, Object>>) topResultTransitTypes;
        assertEquals(List.of(
                Map.of("type", "BUS", "count", 9L),
                Map.of("type", "METRO", "count", 4L)
        ), typeMetrics);
    }
}
