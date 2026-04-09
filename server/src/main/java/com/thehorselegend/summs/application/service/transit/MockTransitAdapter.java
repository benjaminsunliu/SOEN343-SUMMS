package com.thehorselegend.summs.application.service.transit;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MockTransitAdapter implements TransitService{
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    //Status data
    private static final List<TransitLineStatusDTO> LINE_STATUSES = List.of(
        TransitLineStatusDTO.builder()
            .lineId("M1").lineNumber("1").lineName("Green Line")
            .type("METRO").status("ON_TIME")
            .statusMessage("Normal service").lineColor("#009A44").build(),

        TransitLineStatusDTO.builder()
            .lineId("M2").lineNumber("2").lineName("Orange Line")
            .type("METRO").status("DELAYED")
            .statusMessage("5-min delays at Berri-UQAM").lineColor("#F4821F").build(),

        TransitLineStatusDTO.builder()
            .lineId("M4").lineNumber("4").lineName("Yellow Line")
            .type("METRO").status("ON_TIME")
            .statusMessage("Normal service").lineColor("#FFD100").build(),

        TransitLineStatusDTO.builder()
            .lineId("M5").lineNumber("5").lineName("Blue Line")
            .type("METRO").status("DISRUPTED")
            .statusMessage("Service suspended between Snowdon and De La Savane")
            .lineColor("#0060A9").build(),

        TransitLineStatusDTO.builder()
            .lineId("R1").lineNumber("REM").lineName("REM A")
            .type("REM").status("ON_TIME")
            .statusMessage("Normal service").lineColor("#00B5E2").build(),

        TransitLineStatusDTO.builder()
            .lineId("B747").lineNumber("747").lineName("YUL Express")
            .type("BUS").status("ON_TIME")
            .statusMessage("Normal service").lineColor("#E40520").build(),

        TransitLineStatusDTO.builder()
            .lineId("B80").lineNumber("80").lineName("Avenue du Parc")
            .type("BUS").status("DELAYED")
            .statusMessage("10-min delays due to road work").lineColor("#E40520").build()
    );

    //Route templates
    private List<TransitRouteDTO> buildRoutes(TransitSearchRequestDTO req) {
        String baseTime = (req.getTime() != null && !req.getTime().isBlank())
                ? req.getTime() : "09:00";
        LocalTime t = LocalTime.parse(baseTime, FMT);

        return List.of(
            TransitRouteDTO.builder()
                .routeId(1L).lineNumber("1").lineName("Green Line")
                .type("METRO").lineColor("#009A44")
                .origin(req.getOrigin() != null ? req.getOrigin() : "Berri-UQAM")
                .destination(req.getDestination() != null ? req.getDestination() : "Atwater")
                .departureTime(t.format(FMT))
                .arrivalTime(t.plusMinutes(12).format(FMT))
                .durationMinutes(12).transfers(0).fare(3.75)
                .status("ON_TIME").statusMessage("On time")
                .stops(List.of("Berri-UQAM", "Saint-Laurent", "Place-des-Arts",
                               "McGill", "Peel", "Atwater"))
                .build(),

            TransitRouteDTO.builder()
                .routeId(2L).lineNumber("2").lineName("Orange Line")
                .type("METRO").lineColor("#F4821F")
                .origin(req.getOrigin() != null ? req.getOrigin() : "Bonaventure")
                .destination(req.getDestination() != null ? req.getDestination() : "Snowdon")
                .departureTime(t.plusMinutes(4).format(FMT))
                .arrivalTime(t.plusMinutes(22).format(FMT))
                .durationMinutes(18).transfers(1).fare(3.75)
                .status("DELAYED").statusMessage("5-min delays at Berri-UQAM")
                .stops(List.of("Bonaventure", "Lucien-L'Allier", "Georges-Vanier",
                               "Lionel-Groulx", "Vendome", "Snowdon"))
                .build(),

            TransitRouteDTO.builder()
                .routeId(3L).lineNumber("747").lineName("YUL Express")
                .type("BUS").lineColor("#E40520")
                .origin(req.getOrigin() != null ? req.getOrigin() : "Lionel-Groulx")
                .destination(req.getDestination() != null ? req.getDestination() : "YUL Airport")
                .departureTime(t.plusMinutes(7).format(FMT))
                .arrivalTime(t.plusMinutes(52).format(FMT))
                .durationMinutes(45).transfers(0).fare(11.00)
                .status("ON_TIME").statusMessage("On time")
                .stops(List.of("Lionel-Groulx", "Atwater", "Guy-Concordia",
                               "Peel", "Dorchester / Peel", "YUL Airport"))
                .build(),

            TransitRouteDTO.builder()
                .routeId(4L).lineNumber("80").lineName("Avenue du Parc")
                .type("BUS").lineColor("#E40520")
                .origin(req.getOrigin() != null ? req.getOrigin() : "Mont-Royal")
                .destination(req.getDestination() != null ? req.getDestination() : "Place-du-Parc")
                .departureTime(t.plusMinutes(2).format(FMT))
                .arrivalTime(t.plusMinutes(24).format(FMT))
                .durationMinutes(22).transfers(0).fare(3.75)
                .status("DELAYED").statusMessage("10-min delays due to road work")
                .stops(List.of("Mont-Royal", "Laurier", "Saint-Viateur",
                               "Bernard", "Rosemont", "Place-du-Parc"))
                .build(),

            TransitRouteDTO.builder()
                .routeId(5L).lineNumber("REM").lineName("REM A")
                .type("REM").lineColor("#00B5E2")
                .origin(req.getOrigin() != null ? req.getOrigin() : "Gare Centrale")
                .destination(req.getDestination() != null ? req.getDestination() : "Brossard")
                .departureTime(t.plusMinutes(10).format(FMT))
                .arrivalTime(t.plusMinutes(28).format(FMT))
                .durationMinutes(18).transfers(0).fare(3.75)
                .status("ON_TIME").statusMessage("On time")
                .stops(List.of("Gare Centrale", "Île-des-Sœurs", "Panama", "Brossard"))
                .build()
        );
    }

    @Override
    public List<TransitRouteDTO> searchRoutes(TransitSearchRequestDTO request) {
        return buildRoutes(request).stream()
            .filter(r -> {
                if (request.getType() == null || request.getType().equalsIgnoreCase("ALL"))
                    return true;
                return r.getType().equalsIgnoreCase(request.getType());
            })
            .sorted(Comparator.comparing(TransitRouteDTO::getDepartureTime))
            .collect(Collectors.toList());
    }

    @Override
    public List<TransitLineStatusDTO> getLineStatuses() {
        return LINE_STATUSES;
    }
}
