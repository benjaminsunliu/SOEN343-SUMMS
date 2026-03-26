package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.VehicleResponse;
import com.thehorselegend.summs.domain.vehicle.Car;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.BicycleRepository;
import com.thehorselegend.summs.infrastructure.persistence.CarRepository;
import com.thehorselegend.summs.infrastructure.persistence.ScooterRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleEntity;
import com.thehorselegend.summs.infrastructure.persistence.VehicleMapper;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BicycleRepository bicycleRepository;

    @Mock
    private ScooterRepository scooterRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleEntity carEntity;

    @BeforeEach
    void setUp() {
        Car car = new Car(
                1L,
                VehicleStatus.AVAILABLE,
                new Location(45.5017, -73.5673),
                77L,
                0.50,
                "SUMMS-123",
                4
        );
        carEntity = VehicleMapper.toEntity(car);
    }

    @Test
    void getAllVehiclesReturnsCoordinatesWithoutAddressMetadata() {
        when(vehicleRepository.findAll()).thenReturn(List.of(carEntity));

        List<VehicleResponse> responses = vehicleService.getAllVehicles();

        assertEquals(1, responses.size());
        assertNull(responses.get(0).locationAddress());
        assertNull(responses.get(0).locationCity());
    }

    @Test
    void getVehicleByIdReturnsCoordinatesWithoutAddressMetadata() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(carEntity));

        VehicleResponse response = vehicleService.getVehicleById(1L);

        assertNull(response.locationAddress());
        assertNull(response.locationCity());
    }
}
