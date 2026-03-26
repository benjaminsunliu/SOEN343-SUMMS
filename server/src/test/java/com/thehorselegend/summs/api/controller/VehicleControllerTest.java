package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.ContextAwareVehicleResponse;
import com.thehorselegend.summs.api.dto.ContextAwareVehicleSearchResponse;
import com.thehorselegend.summs.api.dto.LocationDto;
import com.thehorselegend.summs.application.service.VehicleSearchService;
import com.thehorselegend.summs.application.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleSearchService vehicleSearchService;

    @MockBean
    private WeatherService weatherService;

    private ContextAwareVehicleSearchResponse mockSearchResponse() {
        ContextAwareVehicleResponse vehicle = new ContextAwareVehicleResponse(
                1L,
                "CAR",
                "AVAILABLE",
                new LocationDto(45.5017, -73.5673),
                null,
                null,
                2L,
                0.5,
                null,
                "TEST1234",
                4,
                false,
                null
        );
        return new ContextAwareVehicleSearchResponse(
                "Clear",
                "LOW",
                "Weather conditions are currently favorable.",
                List.of(vehicle)
        );
    }

    @Test
    void testSearchVehicles_withoutType() throws Exception {
        Mockito.when(vehicleSearchService.searchVehicles(45.5, -73.5, 500, null))
                .thenReturn(mockSearchResponse());

        mockMvc.perform(get("/api/vehicles/search")
                        .param("lat", "45.5")
                        .param("lon", "-73.5")
                        .param("radiusKm", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weatherType").value("Clear"))
                .andExpect(jsonPath("$.vehicles[0].id").value(1))
                .andExpect(jsonPath("$.vehicles[0].type").value("CAR"));
    }

    @Test
    void testSearchVehicles_withTypeFilter() throws Exception {
        Mockito.when(vehicleSearchService.searchVehicles(45.5, -73.5, 500, "car"))
                .thenReturn(mockSearchResponse());

        mockMvc.perform(get("/api/vehicles/search")
                        .param("lat", "45.5")
                        .param("lon", "-73.5")
                        .param("radiusKm", "500")
                        .param("type", "car"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicles[0].type").value("CAR"));
    }
}
