package com.thehorselegend.summs.infrastructure.adapter;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import com.thehorselegend.summs.application.service.transit.TransitService;
import com.thehorselegend.summs.infrastructure.config.GoogleMapsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class GoogleTransitAdapter implements TransitService{
    private final GoogleMapsProperties googleConfig;
    private final RestTemplate restTemplate;

    private static final String DIRECTIONS_URL =
        "https://maps.googleapis.com/maps/api/directions/json" +
        "?origin={origin}&destination={destination}" +
        "&mode=transit&departure_time={depTime}" +
        "&key={key}&language=en&region=ca";

    private static final ZoneId MONTREAL_TZ = ZoneId.of("America/Montreal");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    //search routes

    @Override
    @SuppressWarnings("unchecked")
    public List<TransitRouteDTO> searchRoutes(TransitSearchRequestDTO request) {
        try {
            String origin      = request.getOrigin();
            String destination = request.getDestination();
            String dateStr     = request.getDate();
            String timeStr     = request.getTime();

            if (origin == null || origin.isBlank() ||
                destination == null || destination.isBlank()) {
                return Collections.emptyList();
            }

            // Build epoch departure time
            long departureEpoch = buildDepartureEpoch(dateStr, timeStr);

            log.info("Google Directions: '{}' → '{}' at epoch {}", 
                origin, destination, departureEpoch);

            String url = DIRECTIONS_URL
                .replace("{origin}",      encode(origin + " Montreal Quebec"))
                .replace("{destination}", encode(destination + " Montreal Quebec"))
                .replace("{depTime}",     String.valueOf(departureEpoch))
                .replace("{key}",         googleConfig.getKey());

            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, null, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) return Collections.emptyList();

            String status = (String) body.get("status");
            log.info("Google Directions status: {}", status);

            if (!"OK".equals(status)) {
                log.warn("Google Directions returned status: {}", status);
                return Collections.emptyList();
            }

            List<Map<String, Object>> routes =
                (List<Map<String, Object>>) body.get("routes");
            if (routes == null || routes.isEmpty()) return Collections.emptyList();

            List<TransitRouteDTO> results = new ArrayList<>();
            long idCounter = 1L;

            for (Map<String, Object> route : routes) {
                List<Map<String, Object>> legs =
                    (List<Map<String, Object>>) route.get("legs");
                if (legs == null || legs.isEmpty()) continue;

                Map<String, Object> leg = legs.get(0);
                List<Map<String, Object>> steps =
                    (List<Map<String, Object>>) leg.get("steps");
                if (steps == null) continue;

                // Parse each step — only show transit steps
                for (Map<String, Object> step : steps) {
                    if (!"TRANSIT".equals(step.get("travel_mode"))) continue;

                    Map<String, Object> transitDetails =
                        (Map<String, Object>) step.get("transit_details");
                    if (transitDetails == null) continue;

                    Map<String, Object> line =
                        (Map<String, Object>) transitDetails.get("line");
                    Map<String, Object> depStop =
                        (Map<String, Object>) transitDetails.get("departure_stop");
                    Map<String, Object> arrStop =
                        (Map<String, Object>) transitDetails.get("arrival_stop");
                    Map<String, Object> depTime =
                        (Map<String, Object>) transitDetails.get("departure_time");
                    Map<String, Object> arrTime =
                        (Map<String, Object>) transitDetails.get("arrival_time");
                    Object numStops = transitDetails.get("num_stops");

                    if (line == null || depStop == null || arrStop == null) continue;

                    String lineName    = (String) line.getOrDefault("name", "");
                    String lineShort   = (String) line.getOrDefault("short_name", lineName);
                    String lineColor   = "#" + line.getOrDefault("color", "E40520")
                        .toString().replace("#", "");
                    String vehicleType = "";
                    Map<String, Object> vehicle = (Map<String, Object>) line.get("vehicle");
                    if (vehicle != null) {
                        vehicleType = (String) vehicle.getOrDefault("type", "BUS");
                    }

                    String depTimeTxt = depTime != null ? (String) depTime.get("text") : "";
                    String arrTimeTxt = arrTime != null ? (String) arrTime.get("text") : "";

                    // Duration for this step
                    Map<String, Object> duration =
                        (Map<String, Object>) step.get("duration");
                    int durationMins = duration != null
                        ? ((Number) duration.get("value")).intValue() / 60 : 0;

                    String type = resolveType(vehicleType);
                    String[] lineInfo = resolveLineInfo(lineShort, lineName, lineColor, type);

                    results.add(TransitRouteDTO.builder()
                        .routeId(idCounter++)
                        .lineNumber(lineShort)
                        .lineName(lineInfo[0])
                        .type(type)
                        .lineColor(lineInfo[1])
                        .origin((String) depStop.getOrDefault("name", origin))
                        .destination((String) arrStop.getOrDefault("name", destination))
                        .departureTime(depTimeTxt)
                        .arrivalTime(arrTimeTxt)
                        .durationMinutes(durationMins)
                        .transfers(0)
                        .fare(3.75)
                        .status("ON_TIME")
                        .statusMessage("On time")
                        .stops(buildStopList(depStop, arrStop, numStops))
                        .build());
                }

                //Add full journey as summary if multiple stops
                if (results.isEmpty()) {
                    Map<String, Object> totalDuration =
                        (Map<String, Object>) leg.get("duration");
                    int totalMins = totalDuration != null
                        ? ((Number) totalDuration.get("value")).intValue() / 60 : 0;

                    Map<String, Object> depTimeRaw =
                        (Map<String, Object>) leg.get("departure_time");
                    Map<String, Object> arrTimeRaw =
                        (Map<String, Object>) leg.get("arrival_time");

                    results.add(TransitRouteDTO.builder()
                        .routeId(idCounter++)
                        .lineNumber("Transit")
                        .lineName("Transit Route")
                        .type("BUS")
                        .lineColor("#E40520")
                        .origin(origin)
                        .destination(destination)
                        .departureTime(depTimeRaw != null
                            ? (String) depTimeRaw.get("text") : "")
                        .arrivalTime(arrTimeRaw != null
                            ? (String) arrTimeRaw.get("text") : "")
                        .durationMinutes(totalMins)
                        .transfers(0)
                        .fare(3.75)
                        .status("ON_TIME")
                        .statusMessage("On time")
                        .stops(List.of(origin, destination))
                        .build());
                }

                if (results.size() >= 5) break;
            }

            log.info("Returning {} transit steps", results.size());
            return results;

        } catch (Exception e) {
            log.error("Google Directions failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    //getLineStatuses (using STM real data)

    @Override
    public List<TransitLineStatusDTO> getLineStatuses() {
        // Keep returning the STM line statuses from real adapter
        return defaultLineStatuses();
    }

    //helpers

    private long buildDepartureEpoch(String dateStr, String timeStr) {
        try {
            LocalDate date = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now(MONTREAL_TZ);
            LocalTime time = (timeStr != null && !timeStr.isBlank())
                ? LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                : LocalTime.now(MONTREAL_TZ);
            return LocalDateTime.of(date, time)
                .atZone(MONTREAL_TZ)
                .toEpochSecond();
        } catch (Exception e) {
            return Instant.now().getEpochSecond();
        }
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s.replace(" ", "+");
        }
    }

    private String resolveType(String vehicleType) {
        if (vehicleType == null) return "BUS";
        return switch (vehicleType.toUpperCase()) {
            case "SUBWAY", "METRO_RAIL", "HEAVY_RAIL" -> "METRO";
            case "COMMUTER_TRAIN", "RAIL"              -> "TRAIN";
            case "TRAM", "LIGHT_RAIL"                  -> "REM";
            default                                    -> "BUS";
        };
    }

    private String[] resolveLineInfo(String shortName, String lineName,
                                     String color, String type) {
        // STM Metro lines by short name
        return switch (shortName) {
            case "1" -> new String[]{"Green Line",  "#009A44"};
            case "2" -> new String[]{"Orange Line", "#F4821F"};
            case "4" -> new String[]{"Yellow Line", "#FFD100"};
            case "5" -> new String[]{"Blue Line",   "#0060A9"};
            default  -> new String[]{
                lineName.isBlank() ? "Bus " + shortName : lineName,
                color.isBlank() ? "#E40520" : color
            };
        };
    }

    @SuppressWarnings("unchecked")
    private List<String> buildStopList(Map<String, Object> dep,
                                        Map<String, Object> arr,
                                        Object numStops) {
        List<String> stops = new ArrayList<>();
        stops.add((String) dep.getOrDefault("name", "Origin"));
        if (numStops instanceof Number n && n.intValue() > 2) {
            stops.add("... " + (n.intValue() - 2) + " intermediate stops");
        }
        stops.add((String) arr.getOrDefault("name", "Destination"));
        return stops;
    }

    private List<TransitLineStatusDTO> defaultLineStatuses() {
        return List.of(
            build("M1", "1",   "Green Line",     "METRO", "ON_TIME", "Normal service", "#009A44"),
            build("M2", "2",   "Orange Line",    "METRO", "ON_TIME", "Normal service", "#F4821F"),
            build("M4", "4",   "Yellow Line",    "METRO", "ON_TIME", "Normal service", "#FFD100"),
            build("M5", "5",   "Blue Line",      "METRO", "ON_TIME", "Normal service", "#0060A9"),
            build("R1", "REM", "REM A",          "REM",   "ON_TIME", "Normal service", "#00B5E2"),
            build("B747","747","YUL Express",    "BUS",   "ON_TIME", "Normal service", "#E40520"),
            build("B80", "80", "Avenue du Parc", "BUS",   "ON_TIME", "Normal service", "#E40520")
        );
    }

    private TransitLineStatusDTO build(String id, String num, String name,
            String type, String status, String msg, String color) {
        return TransitLineStatusDTO.builder()
            .lineId(id).lineNumber(num).lineName(name)
            .type(type).status(status).statusMessage(msg).lineColor(color)
            .build();
    }
}
