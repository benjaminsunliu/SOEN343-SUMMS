package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.application.service.reservation.VehicleReservationService;
import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.user.UserRole;
import com.thehorselegend.summs.domain.vehicle.*;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleReservationService reservationService;

    private VehicleEntity vehicle;
    private UserEntity user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        vehicle = new VehicleEntity() {};
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        user = new UserEntity(
                1L,
                "Test User",
                "test@example.com",
                "password123",
                UserRole.CITIZEN
        );

        reservationService = new VehicleReservationService(reservationRepository, vehicleRepository);
    }

    @Test
    void testReserveVehicle_success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservationRepository.findByVehicleAndStatus(vehicle, ReservationStatus.CONFIRMED))
                .thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicle);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Location start = new Location(45.5017, -73.5673);
        Location end = new Location(45.508, -73.561);

        Reservation reservation = reservationService.reserveVehicle(
                user, 1L, start, end, "Montreal",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)
        );

        assertNotNull(reservation);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(vehicle, reservation.getVehicle());
        assertEquals(user, reservation.getUser());
        assertEquals(VehicleStatus.RESERVED, vehicle.getStatus());
    }

    @Test
    void testReserveVehicle_alreadyReserved_throws() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        Reservation existing = new Reservation();
        when(reservationRepository.findByVehicleAndStatus(vehicle, ReservationStatus.CONFIRMED))
                .thenReturn(Optional.of(existing));

        Location start = new Location(45.5017, -73.5673);
        Location end = new Location(45.508, -73.561);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reservationService.reserveVehicle(user, 1L, start, end, "Montreal",
                        LocalDateTime.now(), LocalDateTime.now().plusHours(1))
        );
        assertEquals("Vehicle is already reserved", ex.getMessage());
    }

    @Test
    void testCancelReservation() {
        VehicleEntity vehicle = new BicycleEntity();
        vehicle.setStatus(VehicleStatus.RESERVED);

        UserEntity user = new UserEntity(
                1L,
                "Test User",
                "test@example.com",
                "hashedPassword",
                UserRole.CITIZEN
        );

        Reservation reservation = new Reservation();
        reservation.setVehicle(vehicle);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicle);

        reservationService.cancelReservation(1L, user);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
    }
}