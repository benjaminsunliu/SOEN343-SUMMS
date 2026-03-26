package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.ContextAwareVehicleResponse;
import com.thehorselegend.summs.api.dto.ContextAwareVehicleSearchResponse;
import com.thehorselegend.summs.api.weather.Severity;
import com.thehorselegend.summs.api.weather.WeatherCondition;
import com.thehorselegend.summs.domain.vehicle.Car;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class VehicleSearchServiceTest {
    private VehicleRepository vehicleRepository;
    private WeatherService weatherService;
    private VehicleSearchService vehicleSearchService;

    @BeforeEach
    void setup() {
        vehicleRepository = Mockito.mock(VehicleRepository.class);
        weatherService = Mockito.mock(WeatherService.class);
        vehicleSearchService = new VehicleSearchService(vehicleRepository, weatherService);
    }

    @Test
    void testFindNearbyVehicles() {
        VehicleEntity carEntity = new CarEntity();
        carEntity.setId(1L);
        carEntity.setStatus(VehicleStatus.AVAILABLE);
        carEntity.setLocation(new LocationEmbeddable(45.5017, -73.5673));

        Mockito.when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(carEntity));

        List<Vehicle> result = vehicleSearchService.findNearbyVehicles(45.5, -73.5, 500);

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0) instanceof Car);
    }

    @Test
    void testSearchVehicles_withWeatherHigh() {
        VehicleEntity carEntity = new CarEntity();
        carEntity.setId(1L);
        carEntity.setStatus(VehicleStatus.AVAILABLE);
        carEntity.setLocation(new LocationEmbeddable(45.5017, -73.5673));

        VehicleEntity scooterEntity = new ScooterEntity();
        scooterEntity.setId(2L);
        scooterEntity.setStatus(VehicleStatus.AVAILABLE);
        scooterEntity.setLocation(new LocationEmbeddable(45.5017, -73.5673));

        Mockito.when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(carEntity, scooterEntity));

        Mockito.when(weatherService.getCurrentWeather(45.5, -73.5))
                .thenReturn(new WeatherCondition("Rain", Severity.HIGH));

        ContextAwareVehicleSearchResponse searchResponse =
                vehicleSearchService.searchVehicles(45.5, -73.5, 500, null);

        Assertions.assertEquals("Rain", searchResponse.weatherType());
        Assertions.assertEquals("HIGH", searchResponse.weatherSeverity());
        Assertions.assertEquals(2, searchResponse.vehicles().size());

        ContextAwareVehicleResponse carResponse = searchResponse.vehicles().stream()
                .filter(vehicle -> "CAR".equals(vehicle.type()))
                .findFirst()
                .orElseThrow();
        Assertions.assertFalse(Boolean.TRUE.equals(carResponse.weatherRisky()));

        ContextAwareVehicleResponse scooterResponse = searchResponse.vehicles().stream()
                .filter(vehicle -> "SCOOTER".equals(vehicle.type()))
                .findFirst()
                .orElseThrow();
        Assertions.assertTrue(Boolean.TRUE.equals(scooterResponse.weatherRisky()));
        Assertions.assertNotNull(scooterResponse.weatherRiskMessage());
    }
}
