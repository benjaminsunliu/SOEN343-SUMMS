package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
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

        ParkingReservationEntity savedEntity = ParkingReservationEntity.builder()
                .id(11L)
                .facilityId(77L)
                .facilityName("Downtown Garage")
                .facilityAddress("123 Main St")
                .city("Montreal")
                .arrivalDate("2026-03-27")
                .arrivalTime("14:00")
                .durationHours(4)
                .totalCost(18.0)
                .userId(5L)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.of(2026, 3, 26, 10, 15))
                .build();

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
        ParkingReservationEntity reservation = ParkingReservationEntity.builder()
                .id(21L)
                .facilityId(3L)
                .facilityName("Old Port Parking")
                .facilityAddress("45 Harbor Rd")
                .city("Montreal")
                .arrivalDate("2026-03-30")
                .arrivalTime("09:30")
                .durationHours(2)
                .totalCost(7.5)
                .userId(9L)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.of(2026, 3, 26, 12, 30))
                .build();

        when(parkingReservationRepository.findByUserIdOrderByCreatedAtDesc(9L))
                .thenReturn(List.of(reservation));

        List<ParkingReservationResponse> responses = parkingReservationService.getUserReservations(9L);

        assertEquals(1, responses.size());
        assertEquals(21L, responses.get(0).getReservationId());
        assertEquals("Old Port Parking", responses.get(0).getFacilityName());
    }

    @Test
    void cancelReservationMarksReservationAsCancelled() {
        ParkingReservationEntity reservation = ParkingReservationEntity.builder()
                .id(31L)
                .userId(4L)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.of(2026, 3, 26, 9, 0))
                .build();

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
        ParkingReservationEntity reservation = ParkingReservationEntity.builder()
                .id(32L)
                .userId(8L)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.of(2026, 3, 26, 9, 0))
                .build();

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
