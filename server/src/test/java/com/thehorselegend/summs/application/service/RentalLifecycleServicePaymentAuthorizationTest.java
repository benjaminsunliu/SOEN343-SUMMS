package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.StartTripRequest;
import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.application.service.payment.PaymentResult;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.infrastructure.persistence.ReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.ReservationRepository;
import com.thehorselegend.summs.infrastructure.persistence.TripRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalLifecycleServicePaymentAuthorizationTest {

    private static final Long CITIZEN_ID = 9L;
    private static final Long RESERVATION_ID = 10L;
    private static final Long VEHICLE_ID = 33L;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private IPaymentService paymentService;

    @InjectMocks
    private RentalLifecycleService rentalLifecycleService;

    @Test
    void startTripRejectsWhenPaymentServiceDeclines() {
        VehicleReservation reservation = new VehicleReservation(
                RESERVATION_ID,
                CITIZEN_ID,
                VEHICLE_ID,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusMinutes(30),
                "Montreal",
                ReservationStatus.CONFIRMED,
                new Location(45.5017, -73.5673),
                new Location(45.5020, -73.5700)
        );

        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(ReservationMapper.toEntity(reservation)));
        when(paymentService.processPayment("Tokenized Checkout", "PAY-DECLINED", 1.00))
                .thenReturn(PaymentResult.declined());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> rentalLifecycleService.startTrip(CITIZEN_ID, new StartTripRequest(RESERVATION_ID, "PAY-DECLINED"))
        );

        assertEquals("Payment authorization failed.", error.getMessage());
    }
}
