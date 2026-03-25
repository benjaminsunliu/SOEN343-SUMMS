package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.application.service.VehicleSearchService;
import com.thehorselegend.summs.application.service.WeatherService;
import com.thehorselegend.summs.domain.vehicle.Car;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
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

    @Test
    void testSearchVehicles_withoutType() throws Exception {
        Location loc = new Location(45.5017, -73.5673);
        Vehicle car = new Car(1L, VehicleStatus.AVAILABLE, loc, 2L, 0.5, "TEST1234", 4);
        List<Vehicle> vehicles = List.of(car);

        Mockito.when(vehicleSearchService.searchVehicles(45.5, -73.5, 500, null))
                .thenReturn(vehicles);

        mockMvc.perform(get("/api/vehicles/search")
                        .param("lat", "45.5")
                        .param("lon", "-73.5")
                        .param("radiusKm", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("CAR"));
    }

    @Test
    void testSearchVehicles_withTypeFilter() throws Exception {
        Location loc = new Location(45.5017, -73.5673);
        Vehicle car = new Car(1L, VehicleStatus.AVAILABLE, loc, 2L, 0.5, "TEST1234", 4);
        List<Vehicle> vehicles = List.of(car);

        Mockito.when(vehicleSearchService.searchVehicles(45.5, -73.5, 500, "car"))
                .thenReturn(vehicles);

        mockMvc.perform(get("/api/vehicles/search")
                        .param("lat", "45.5")
                        .param("lon", "-73.5")
                        .param("radiusKm", "500")
                        .param("type", "car"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CAR"));
    }
}