package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.EndTripRequest;
import com.thehorselegend.summs.api.dto.LocationDto;
import com.thehorselegend.summs.api.dto.StartTripRequest;
import com.thehorselegend.summs.api.dto.TripResponse;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.vehicle.Car;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.ReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.ReservationRepository;
import com.thehorselegend.summs.infrastructure.persistence.TripEntity;
import com.thehorselegend.summs.infrastructure.persistence.TripRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleEntity;
import com.thehorselegend.summs.infrastructure.persistence.VehicleMapper;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import com.thehorselegend.summs.shared.time.SummsTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalLifecycleServiceTest {

    private static final Long CITIZEN_ID = 7L;
    private static final Long RESERVATION_ID = 11L;
    private static final Long VEHICLE_ID = 19L;
    private static final Long TRIP_ID = 29L;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RentalLifecycleService rentalLifecycleService;

    private VehicleReservation confirmedReservation;
    private ReservationEntity confirmedReservationEntity;
    private VehicleEntity reservedVehicleEntity;
    private LocalDateTime referenceNow;

    @BeforeEach
    void setUp() {
        referenceNow = SummsTime.now();
        confirmedReservation = new VehicleReservation(
                RESERVATION_ID,
                CITIZEN_ID,
                VEHICLE_ID,
                referenceNow.minusMinutes(5),
                referenceNow.plusMinutes(15),
                "Montreal",
                com.thehorselegend.summs.domain.reservation.ReservationStatus.CONFIRMED,
                new Location(45.5017, -73.5673),
                new Location(45.5017, -73.5673)
        );
        confirmedReservationEntity = ReservationMapper.toEntity(confirmedReservation);

        Car reservedVehicle = new Car(
                VEHICLE_ID,
                VehicleStatus.RESERVED,
                new Location(45.5017, -73.5673),
                101L,
                0.45,
                "SUMMS-123",
                4
        );
        reservedVehicleEntity = VehicleMapper.toEntity(reservedVehicle);
    }

    @Test
    void startTripActivatesReservationAndMovesVehicleToInUse() {
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(confirmedReservationEntity));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(reservedVehicleEntity));
        when(tripRepository.findByVehicleIdAndEndTimeIsNull(VEHICLE_ID)).thenReturn(Optional.empty());
        when(tripRepository.findByCitizenIdAndEndTimeIsNull(CITIZEN_ID)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(VehicleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripRepository.save(any(TripEntity.class))).thenAnswer(invocation -> {
            TripEntity trip = invocation.getArgument(0);
            return new TripEntity(
                    TRIP_ID,
                    trip.getReservationId(),
                    trip.getVehicleId(),
                    trip.getCitizenId(),
                    trip.getStartTime(),
                    trip.getEndTime(),
                    trip.getTotalDurationMinutes()
            );
        });

        TripResponse response = rentalLifecycleService.startTrip(
                CITIZEN_ID,
                new StartTripRequest(RESERVATION_ID, "PAY-READY")
        );

        assertEquals(TRIP_ID, response.tripId());
        assertEquals(RESERVATION_ID, response.reservationId());
        assertEquals(VehicleStatus.IN_USE.name(), response.vehicleStatus());
        assertNotNull(response.startTime());

        ArgumentCaptor<ReservationEntity> reservationCaptor = ArgumentCaptor.forClass(ReservationEntity.class);
        ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        verify(vehicleRepository).save(vehicleCaptor.capture());

        assertEquals("ACTIVE", reservationCaptor.getValue().getStatus().name());
        assertEquals(VehicleStatus.IN_USE, vehicleCaptor.getValue().getStatus());
    }

    @Test
    void startTripRejectsExpiredReservation() {
        VehicleReservation expiredReservation = new VehicleReservation(
                RESERVATION_ID,
                CITIZEN_ID,
                VEHICLE_ID,
                referenceNow.minusHours(2),
                referenceNow.minusMinutes(1),
                "Montreal",
                com.thehorselegend.summs.domain.reservation.ReservationStatus.CONFIRMED,
                new Location(45.5017, -73.5673),
                new Location(45.5017, -73.5673)
        );

        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(ReservationMapper.toEntity(expiredReservation)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> rentalLifecycleService.startTrip(CITIZEN_ID, new StartTripRequest(RESERVATION_ID, "PAY-READY"))
        );

        assertEquals("Reservation is expired.", error.getMessage());
    }

    @Test
    void endTripCompletesTripAndReleasesVehicleInValidZone() {
        Car inUseVehicle = new Car(
                VEHICLE_ID,
                VehicleStatus.IN_USE,
                new Location(45.5017, -73.5673),
                101L,
                0.45,
                "SUMMS-123",
                4
        );

        TripEntity activeTripEntity = new TripEntity(
                TRIP_ID,
                RESERVATION_ID,
                VEHICLE_ID,
                CITIZEN_ID,
                referenceNow.minusMinutes(25),
                null,
                null
        );
        VehicleReservation activeReservation = new VehicleReservation(
                RESERVATION_ID,
                CITIZEN_ID,
                VEHICLE_ID,
                referenceNow.minusHours(1),
                referenceNow.plusHours(1),
                "Montreal",
                com.thehorselegend.summs.domain.reservation.ReservationStatus.ACTIVE,
                new Location(45.5017, -73.5673),
                new Location(45.5017, -73.5673)
        );

        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(activeTripEntity));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(VehicleMapper.toEntity(inUseVehicle)));
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(ReservationMapper.toEntity(activeReservation)));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripRepository.save(any(TripEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripResponse response = rentalLifecycleService.endTrip(
                CITIZEN_ID,
                TRIP_ID,
                new EndTripRequest(new LocationDto(45.5050, -73.5700))
        );

        assertEquals(TRIP_ID, response.tripId());
        assertEquals(VehicleStatus.AVAILABLE.name(), response.vehicleStatus());
        assertNotNull(response.endTime());
        assertTrue(response.totalDurationMinutes() >= 25L);

        ArgumentCaptor<ReservationEntity> reservationCaptor = ArgumentCaptor.forClass(ReservationEntity.class);
        ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals("COMPLETED", reservationCaptor.getValue().getStatus().name());
        assertEquals(45.5050, reservationCaptor.getValue().getEndLocation().getLatitude());
        assertEquals(-73.5700, reservationCaptor.getValue().getEndLocation().getLongitude());
        assertEquals(VehicleStatus.AVAILABLE, vehicleCaptor.getValue().getStatus());
        assertEquals(45.5050, vehicleCaptor.getValue().getLocation().getLatitude());
        assertEquals(-73.5700, vehicleCaptor.getValue().getLocation().getLongitude());
    }

    @Test
    void endTripRejectsInvalidDropOffZone() {
        TripEntity activeTripEntity = new TripEntity(
                TRIP_ID,
                RESERVATION_ID,
                VEHICLE_ID,
                CITIZEN_ID,
                referenceNow.minusMinutes(10),
                null,
                null
        );
        VehicleReservation activeReservation = new VehicleReservation(
                RESERVATION_ID,
                CITIZEN_ID,
                VEHICLE_ID,
                referenceNow.minusHours(1),
                referenceNow.plusHours(1),
                "Montreal",
                com.thehorselegend.summs.domain.reservation.ReservationStatus.ACTIVE,
                new Location(45.5017, -73.5673),
                new Location(45.5017, -73.5673)
        );

        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(activeTripEntity));
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(ReservationMapper.toEntity(activeReservation)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> rentalLifecycleService.endTrip(
                        CITIZEN_ID,
                        TRIP_ID,
                        new EndTripRequest(new LocationDto(46.0, -74.0))
                )
        );

        assertEquals("Trip cannot be ended outside a valid drop-off zone or your reserved destination.", error.getMessage());
    }

    @Test
    void endTripAllowsDropOffAtReservedDestinationOutsideDefaultZones() {
        TripEntity activeTripEntity = new TripEntity(
                TRIP_ID,
                RESERVATION_ID,
                VEHICLE_ID,
                CITIZEN_ID,
                referenceNow.minusMinutes(10),
                null,
                null
        );
        VehicleReservation activeReservation = new VehicleReservation(
                RESERVATION_ID,
                CITIZEN_ID,
                VEHICLE_ID,
                referenceNow.minusHours(1),
                referenceNow.plusHours(1),
                "Montreal",
                com.thehorselegend.summs.domain.reservation.ReservationStatus.ACTIVE,
                new Location(45.5017, -73.5673),
                new Location(46.0000, -74.0000)
        );
        Car inUseVehicle = new Car(
                VEHICLE_ID,
                VehicleStatus.IN_USE,
                new Location(45.5017, -73.5673),
                101L,
                0.45,
                "SUMMS-123",
                4
        );

        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(activeTripEntity));
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(ReservationMapper.toEntity(activeReservation)));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(VehicleMapper.toEntity(inUseVehicle)));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripRepository.save(any(TripEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripResponse response = rentalLifecycleService.endTrip(
                CITIZEN_ID,
                TRIP_ID,
                new EndTripRequest(new LocationDto(46.0000, -74.0000))
        );

        assertEquals(TRIP_ID, response.tripId());
        assertEquals(VehicleStatus.AVAILABLE.name(), response.vehicleStatus());
    }
}
