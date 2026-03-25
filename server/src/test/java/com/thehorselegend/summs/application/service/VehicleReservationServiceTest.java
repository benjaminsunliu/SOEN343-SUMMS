package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.application.service.reservation.VehicleReservationService;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.vehicle.Car;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.ReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ReservationRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleEntity;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import com.thehorselegend.summs.infrastructure.persistence.ReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.VehicleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class VehicleReservationServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private VehicleReservationService reservationService;

    private Car vehicle;
    private VehicleEntity vehicleEntity;
    private VehicleReservation reservation;
    private ReservationEntity reservationEntity;

    private Long userId = 1L;
    private Long vehicleId = 1L;
    private Location start = new Location(1.0, 2.0);
    private Location end = new Location(3.0, 4.0);

    private LocalDateTime startTime = LocalDateTime.of(2026, 3, 24, 10, 0);
    private LocalDateTime endTime = LocalDateTime.of(2026, 3, 24, 12, 0);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create domain vehicle
        vehicle = new Car(
                vehicleId,
                VehicleStatus.AVAILABLE,
                start,
                100L,        // providerId
                0.5,         // costPerMinute
                "ABC123",    // licensePlate
                4            // seatingCapacity
        );

        // Map to entity
        vehicleEntity = VehicleMapper.toEntity(vehicle);

        // Mock repository behavior
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicleEntity);

        // Create reservation
        reservation = new VehicleReservation(
                userId,
                vehicleId,
                startTime,
                endTime,
                "CITY",
                start,
                end
        );

        // Map to entity
        reservationEntity = ReservationMapper.toEntity(reservation);

        // Mock repository behavior
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(reservationEntity);
    }

    @Test
    void testCreateReservation() {
        VehicleReservation created = (VehicleReservation) reservationService.createReservation(
                vehicle,
                userId,
                startTime,
                endTime
        );

        assertEquals(userId, created.getUserId());
        assertEquals(vehicleId, created.getReservableId());
        assertNotNull(created.getStartLocation());
        assertNotNull(created.getEndLocation());
    }
}