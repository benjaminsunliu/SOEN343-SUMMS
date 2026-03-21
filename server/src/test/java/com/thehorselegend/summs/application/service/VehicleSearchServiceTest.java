package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.weather.Severity;
import com.thehorselegend.summs.api.weather.WeatherCondition;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleSearchServiceTest {

    private VehicleRepository vehicleRepository;
    private WeatherService weatherService;
    private VehicleSearchService vehicleSearchService;

    @BeforeEach
    void setUp() {
        vehicleRepository = mock(VehicleRepository.class);
        weatherService = mock(WeatherService.class);
        vehicleSearchService = new VehicleSearchService(vehicleRepository, weatherService);
    }

    private LocationEmbeddable loc(double lat, double lon) {
        LocationEmbeddable l = new LocationEmbeddable();
        l.setLatitude(lat);
        l.setLongitude(lon);
        return l;
    }

    @Test
    void shouldReturnVehiclesWithinRadius() {
        CarEntity car = new CarEntity();
        car.setId(1L);
        car.setStatus(VehicleStatus.AVAILABLE);
        car.setLocation(loc(45.5017, -73.5673));

        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(car));

        List<Vehicle> result = vehicleSearchService.findNearbyVehicles(45.5, -73.5, 6);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldReturnEmptyListWhenOutsideRadius() {
        CarEntity car = new CarEntity();
        car.setId(1L);
        car.setStatus(VehicleStatus.AVAILABLE);
        car.setLocation(loc(50.0, -80.0));

        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(car));

        List<Vehicle> result = vehicleSearchService.findNearbyVehicles(45.5, -73.5, 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterNonCarsWhenWeatherIsHighSeverity() {
        CarEntity car = new CarEntity();
        car.setId(1L);
        car.setStatus(VehicleStatus.AVAILABLE);
        car.setLocation(loc(45.5017, -73.5673));

        ScooterEntity scooter = new ScooterEntity();
        scooter.setId(2L);
        scooter.setStatus(VehicleStatus.AVAILABLE);
        scooter.setLocation(loc(45.5017, -73.5673));

        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(car, scooter));

        when(weatherService.getCurrentWeather(eq(45.5), eq(-73.5)))
                .thenReturn(new WeatherCondition("Storm", Severity.HIGH));

        List<Vehicle> result = vehicleSearchService.searchVehicles(45.5, -73.5, 6, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldReturnAllVehiclesWhenWeatherNotHigh() {
        CarEntity car = new CarEntity();
        car.setId(1L);
        car.setStatus(VehicleStatus.AVAILABLE);
        car.setLocation(loc(45.5017, -73.5673));

        ScooterEntity scooter = new ScooterEntity();
        scooter.setId(2L);
        scooter.setStatus(VehicleStatus.AVAILABLE);
        scooter.setLocation(loc(45.5017, -73.5673));

        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(car, scooter));

        when(weatherService.getCurrentWeather(eq(45.5), eq(-73.5)))
                .thenReturn(new WeatherCondition("Clouds", Severity.MEDIUM));

        List<Vehicle> result = vehicleSearchService.searchVehicles(45.5, -73.5, 6, null);

        assertEquals(2, result.size());
    }

    @Test
    void shouldFilterByVehicleType() {
        CarEntity car = new CarEntity();
        car.setId(1L);
        car.setStatus(VehicleStatus.AVAILABLE);
        car.setLocation(loc(45.5017, -73.5673));

        ScooterEntity scooter = new ScooterEntity();
        scooter.setId(2L);
        scooter.setStatus(VehicleStatus.AVAILABLE);
        scooter.setLocation(loc(45.5017, -73.5673));

        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(car, scooter));

        when(weatherService.getCurrentWeather(eq(45.5), eq(-73.5)))
                .thenReturn(new WeatherCondition("Clear", Severity.LOW));

        List<Vehicle> result = vehicleSearchService.searchVehicles(45.5, -73.5, 6, "car");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}