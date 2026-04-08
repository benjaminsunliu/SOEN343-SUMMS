package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.application.service.payment.PaymentApplicationService;
import com.thehorselegend.summs.application.service.payment.PaymentTransactionService;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.payment.CreditCardPaymentStrategy;
import com.thehorselegend.summs.infrastructure.payment.PaypalPaymentStrategy;
import com.thehorselegend.summs.infrastructure.payment.WalletPaymentStrategy;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ParkingReservationServiceTest {

    @Mock
    private ParkingReservationRepository parkingReservationRepository;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    private ParkingReservationService parkingReservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PaymentApplicationService paymentApplicationService = new PaymentApplicationService(List.of(
                new CreditCardPaymentStrategy(),
                new PaypalPaymentStrategy(),
                new WalletPaymentStrategy()
        ));
        parkingReservationService =
                new ParkingReservationService(
                        parkingReservationRepository,
                        paymentApplicationService,
                        paymentTransactionService
                );
    }

    @Test
    void createReservationPersistsAndReturnsResponse() {
        CreateParkingReservationRequest request = buildRequest();
        request.setIncludeServiceFee(false);
        request.setServiceFeeAmount(2.5);
        request.setIncludeTax(false);
        request.setTaxRate(0.15);

        when(parkingReservationRepository.save(any(ParkingReservationEntity.class)))
                .thenAnswer(invocation -> {
                    ParkingReservationEntity entity = invocation.getArgument(0);
                    entity.setId(11L);
                    entity.setCreatedAt(LocalDateTime.of(2026, 3, 26, 10, 15));
                    return entity;
                });

        ParkingReservationResponse response = parkingReservationService.createReservation(request, 5L);

        ArgumentCaptor<ParkingReservationEntity> reservationCaptor =
                ArgumentCaptor.forClass(ParkingReservationEntity.class);
        verify(parkingReservationRepository).save(reservationCaptor.capture());

        ParkingReservationEntity savedReservation = reservationCaptor.getValue();
        assertEquals(ReservationStatus.CONFIRMED, savedReservation.getStatus());
        assertEquals(23.575, savedReservation.getTotalCost(), 0.0001);
        verify(paymentTransactionService).recordTransaction(
                eq(11L),
                eq(5L),
                eq(0L),
                eq("CREDIT_CARD"),
                eq(23.575),
                eq(true),
                any(String.class),
                any(String.class),
                any(String.class)
        );

        assertEquals(11L, response.getReservationId());
        assertEquals("Downtown Garage", response.getFacilityName());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(23.575, response.getTotalCost(), 0.0001);
        assertEquals("2026-03-26 10:15", response.getConfirmedAt());
    }

    @Test
    void createReservationThrowsWhenPaymentFails() {
        CreateParkingReservationRequest request = buildRequest();
        request.setCreditCardNumber("not-the-test-card");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parkingReservationService.createReservation(request, 5L)
        );

        assertEquals(
                "Credit card payment declined (use test card 12345678) | amount=18.0 | details=Base reservation payment + service fee + tax",
                exception.getMessage()
        );
        verify(parkingReservationRepository, never()).save(any(ParkingReservationEntity.class));
        verifyNoInteractions(paymentTransactionService);
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
                .facilityId(3L)
                .facilityName("Old Port Parking")
                .facilityAddress("45 Harbor Rd")
                .city("Montreal")
                .arrivalDate("2026-03-30")
                .arrivalTime("09:30")
                .durationHours(2)
                .totalCost(7.5)
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
                .facilityId(3L)
                .facilityName("Old Port Parking")
                .facilityAddress("45 Harbor Rd")
                .city("Montreal")
                .arrivalDate("2026-03-30")
                .arrivalTime("09:30")
                .durationHours(2)
                .totalCost(7.5)
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

    private CreateParkingReservationRequest buildRequest() {
        CreateParkingReservationRequest request = new CreateParkingReservationRequest();
        request.setFacilityId(77L);
        request.setFacilityName("Downtown Garage");
        request.setFacilityAddress("123 Main St");
        request.setCity("Montreal");
        request.setArrivalDate("2026-03-27");
        request.setArrivalTime("14:00");
        request.setDurationHours(4);
        request.setTotalCost(18.0);
        request.setPaymentMethod("CREDIT_CARD");
        request.setCreditCardNumber("12345678");
        return request;
    }
}
