package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.application.service.reservation.ParkingReservationService;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParkingReservationServiceTest {

    @Mock
    private ParkingReservationRepository parkingReservationRepository;

    @InjectMocks
    private ParkingReservationService parkingReservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReservationPersistsAndReturnsResponse() {
        CreateParkingReservationRequest request = new CreateParkingReservationRequest();
        request.setFacilityId(77L);
        request.setFacilityName("Downtown Garage");
        request.setFacilityAddress("123 Main St");
        request.setCity("Montreal");
        request.setArrivalDate("2026-03-27");
        request.setArrivalTime("14:00");
        request.setDurationHours(4);
        request.setTotalCost(18.0);
        request.setPaymentMethod("CREDIT");

        ParkingReservationEntity savedEntity = new ParkingReservationEntity(
                11L,
                5L,
                77L,
                LocalDateTime.of(2026, 3, 27, 14, 0),
                LocalDateTime.of(2026, 3, 27, 18, 0),
                "Montreal",
                ReservationStatus.CONFIRMED,
                "Downtown Garage",
                "123 Main St",
                4,
                18.0,
                LocalDateTime.of(2026, 3, 26, 10, 15)
        );

        when(parkingReservationRepository.save(any(ParkingReservationEntity.class)))
                .thenReturn(savedEntity);

        ParkingReservationResponse response = parkingReservationService.createReservation(request, 5L);

        assertEquals(11L, response.getReservationId());
        assertEquals("Downtown Garage", response.getFacilityName());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals("2026-03-26 10:15", response.getConfirmedAt());
    }

    @Test
    void getUserReservationsReturnsMappedReservations() {
        ParkingReservationEntity reservation = new ParkingReservationEntity(
                21L,
                9L,
                3L,
                LocalDateTime.of(2026, 3, 30, 9, 30),
                LocalDateTime.of(2026, 3, 30, 11, 30),
                "Montreal",
                ReservationStatus.CONFIRMED,
                "Old Port Parking",
                "45 Harbor Rd",
                2,
                7.5,
                LocalDateTime.of(2026, 3, 26, 12, 30)
        );

        when(parkingReservationRepository.findByUserIdOrderByCreatedAtDesc(9L))
                .thenReturn(List.of(reservation));

        List<ParkingReservationResponse> responses = parkingReservationService.getUserReservations(9L);

        assertEquals(1, responses.size());
        assertEquals(21L, responses.get(0).getReservationId());
        assertEquals("Old Port Parking", responses.get(0).getFacilityName());
    }

    @Test
    void cancelReservationMarksReservationAsCancelled() {
        ParkingReservationEntity reservation = new ParkingReservationEntity(
                31L,
                4L,
                3L,
                LocalDateTime.of(2026, 3, 30, 9, 30),
                LocalDateTime.of(2026, 3, 30, 11, 30),
                "Montreal",
                ReservationStatus.CONFIRMED,
                "Old Port Parking",
                "45 Harbor Rd",
                2,
                7.5,
                LocalDateTime.of(2026, 3, 26, 9, 0)
        );

        when(parkingReservationRepository.findById(31L))
                .thenReturn(Optional.of(reservation));

        parkingReservationService.cancelReservation(31L, 4L);

        ArgumentCaptor<ParkingReservationEntity> reservationCaptor =
                ArgumentCaptor.forClass(ParkingReservationEntity.class);
        verify(parkingReservationRepository).save(reservationCaptor.capture());
        assertEquals(ReservationStatus.CANCELLED, reservationCaptor.getValue().getStatus());
    }

    @Test
    void cancelReservationThrowsWhenUserDoesNotOwnReservation() {
        ParkingReservationEntity reservation = new ParkingReservationEntity(
                32L,
                8L,
                3L,
                LocalDateTime.of(2026, 3, 30, 9, 30),
                LocalDateTime.of(2026, 3, 30, 11, 30),
                "Montreal",
                ReservationStatus.CONFIRMED,
                "Old Port Parking",
                "45 Harbor Rd",
                2,
                7.5,
                LocalDateTime.of(2026, 3, 26, 9, 0)
        );

        when(parkingReservationRepository.findById(32L))
                .thenReturn(Optional.of(reservation));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> parkingReservationService.cancelReservation(32L, 4L)
        );

        assertEquals(
                "User not authorized to cancel this parking reservation",
                exception.getMessage()
        );
    }
}