package com.thehorselegend.summs.application.service.transit;

import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogEntity;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchLogRepository;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchResultEntity;
import com.thehorselegend.summs.infrastructure.persistence.TransitSearchResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitSearchServiceTest {

    @Mock
    private TransitService transitService;

    @Mock
    private TransitSearchLogRepository searchLogRepository;

    @Mock
    private TransitSearchResultRepository searchResultRepository;

    private TransitSearchService transitSearchService;

    @BeforeEach
    void setUp() {
        transitSearchService = new TransitSearchService(
                transitService,
                searchLogRepository,
                searchResultRepository
        );
    }

    @Test
    void searchRoutes_savesDistinctNormalizedReturnedTransitTypesOnSearchLog() {
        TransitSearchRequestDTO request = new TransitSearchRequestDTO();
        request.setOrigin("Montreal");
        request.setDestination("Laval");
        request.setDate("2026-04-09");
        request.setTime("14:00");
        request.setType("ALL");

        List<TransitRouteDTO> routes = List.of(
                TransitRouteDTO.builder().type("Bus").lineNumber("24").lineName("Sherbrooke").build(),
                TransitRouteDTO.builder().type(" metro ").lineNumber("1").lineName("Green").build(),
                TransitRouteDTO.builder().type("Bus").lineNumber("410").lineName("Express").build(),
                TransitRouteDTO.builder().type(" ").lineNumber("5").lineName("Ignored").build()
        );

        when(transitService.searchRoutes(request)).thenReturn(routes);
        when(searchLogRepository.save(any(TransitSearchLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(searchResultRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TransitRouteDTO> result = transitSearchService.searchRoutes(request);

        assertEquals(routes, result);

        verify(searchLogRepository).save(argThat(log ->
                "Montreal".equals(log.getOrigin()) &&
                "Laval".equals(log.getDestination()) &&
                "ALL".equals(log.getTransitType()) &&
                Set.of("BUS", "METRO").equals(log.getReturnedTransitTypes())
        ));

        verify(searchResultRepository).saveAll(argThat(entities -> {
            List<TransitSearchResultEntity> savedEntities = (List<TransitSearchResultEntity>) entities;
            return savedEntities.size() == 4
                    && "BUS".equals(savedEntities.get(0).getTransitType())
                    && "METRO".equals(savedEntities.get(1).getTransitType())
                    && "BUS".equals(savedEntities.get(2).getTransitType())
                    && savedEntities.get(3).getTransitType() == null;
        }));
    }

    @Test
    void searchRoutes_savesEmptyReturnedTransitTypesWhenNoRoutesAreFound() {
        TransitSearchRequestDTO request = new TransitSearchRequestDTO();
        request.setType("ALL");

        when(transitService.searchRoutes(request)).thenReturn(List.of());
        when(searchLogRepository.save(any(TransitSearchLogEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TransitRouteDTO> result = transitSearchService.searchRoutes(request);

        assertEquals(List.of(), result);

        verify(searchLogRepository).save(argThat(log ->
                new LinkedHashSet<String>().equals(log.getReturnedTransitTypes())
        ));
        verify(searchResultRepository, never()).saveAll(any());
    }
}
