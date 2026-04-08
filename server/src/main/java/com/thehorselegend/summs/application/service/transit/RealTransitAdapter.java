package com.thehorselegend.summs.application.service.transit;

import com.google.transit.realtime.GtfsRealtime;
import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import com.thehorselegend.summs.infrastructure.config.StmApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RealTransitAdapter implements TransitService{
    private final StmApiProperties stmConfig;
    private final RestTemplate restTemplate;

    //HTTP

    private HttpHeaders apiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apiKey", stmConfig.getKey());
        return headers;
    }

    private byte[] fetchProtobuf(String url) {
        try {
            ResponseEntity<byte[]> res = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(apiHeaders()), byte[].class);
            byte[] body = res.getBody();
            log.info("Fetched {} bytes from {}", body != null ? body.length : 0, url);
            return body != null ? body : new byte[0];
        } catch (Exception e) {
            log.error("Failed to fetch {}: {}", url, e.getMessage());
            return new byte[0];
        }
    }

    // searchRoutes (not userd since GoogleTransitAdapter is @Primary)

    @Override
    public List<TransitRouteDTO> searchRoutes(TransitSearchRequestDTO request) {
        return Collections.emptyList();
    }

    // ── getLineStatuses — real STM alerts via GTFS-RT ─────────────────────────

    @Override
    public List<TransitLineStatusDTO> getLineStatuses() {
        Map<String, TransitLineStatusDTO> statusMap = defaultStatusMap();
        try {
            byte[] bytes = fetchProtobuf(stmConfig.getAlertsUrl());
            if (bytes.length == 0) return new ArrayList<>(statusMap.values());

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(bytes);

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasAlert()) continue;
                GtfsRealtime.Alert alert = entity.getAlert();

                String status  = effectToStatus(alert.getEffect().getNumber());
                String message = alert.hasHeaderText()
                    && alert.getHeaderText().getTranslationCount() > 0
                    ? alert.getHeaderText().getTranslation(0).getText()
                    : "Service disruption";

                for (GtfsRealtime.EntitySelector informed : alert.getInformedEntityList()) {
                    String routeId = informed.getRouteId();
                    if (routeId.isEmpty() || !statusMap.containsKey(routeId)) continue;

                    TransitLineStatusDTO existing = statusMap.get(routeId);
                    statusMap.put(routeId, buildStatus(
                        existing.getLineId(), existing.getLineNumber(),
                        existing.getLineName(), existing.getType(),
                        status, message, existing.getLineColor()));
                }
            }
        } catch (Exception e) {
            log.error("Alert fetch failed: {}", e.getMessage());
        }
        return new ArrayList<>(statusMap.values());
    }

    // Helpers

    private String effectToStatus(int effect) {
        return switch (effect) {
            case 1      -> "DISRUPTED";
            case 2, 5  -> "DELAYED";
            default     -> "ON_TIME";
        };
    }

    private TransitLineStatusDTO buildStatus(String lineId, String lineNumber,
            String lineName, String type, String status, String message, String color) {
        return TransitLineStatusDTO.builder()
            .lineId(lineId).lineNumber(lineNumber).lineName(lineName)
            .type(type).status(status).statusMessage(message).lineColor(color)
            .build();
    }

    private Map<String, TransitLineStatusDTO> defaultStatusMap() {
        Map<String, TransitLineStatusDTO> map = new LinkedHashMap<>();
        map.put("1",   buildStatus("M1",   "1",   "Green Line",     "METRO", "ON_TIME", "Normal service", "#009A44"));
        map.put("2",   buildStatus("M2",   "2",   "Orange Line",    "METRO", "ON_TIME", "Normal service", "#F4821F"));
        map.put("4",   buildStatus("M4",   "4",   "Yellow Line",    "METRO", "ON_TIME", "Normal service", "#FFD100"));
        map.put("5",   buildStatus("M5",   "5",   "Blue Line",      "METRO", "ON_TIME", "Normal service", "#0060A9"));
        map.put("REM", buildStatus("R1",   "REM", "REM A",          "REM",   "ON_TIME", "Normal service", "#00B5E2"));
        map.put("747", buildStatus("B747", "747", "YUL Express",    "BUS",   "ON_TIME", "Normal service", "#E40520"));
        map.put("80",  buildStatus("B80",  "80",  "Avenue du Parc", "BUS",   "ON_TIME", "Normal service", "#E40520"));
        return map;
    }
}
