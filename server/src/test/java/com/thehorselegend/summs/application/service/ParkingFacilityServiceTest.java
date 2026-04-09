package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CityParkingAnalyticsResponse;
import com.thehorselegend.summs.domain.parking.ParkingFacility;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.ParkingFacilityRepository;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingFacilityServiceTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mock
    private ParkingFacilityRepository parkingFacilityRepository;

    @Mock
    private ParkingReservationRepository parkingReservationRepository;

    @InjectMocks
    private ParkingFacilityService parkingFacilityService;

    @Test
    void getProviderAnalytics_aggregatesFacilityOccupancyAndRevenue() {
        ParkingFacility facilityOne = ParkingFacility.builder()
                .facilityId(10L)
                .name("Old Port Garage")
                .city("Montreal")
                .totalSpots(10)
                .providerId(55L)
                .active(Boolean.TRUE)
                .build();
        ParkingFacility facilityTwo = ParkingFacility.builder()
                .facilityId(20L)
                .name("Plateau Parking")
                .city("Montreal")
                .totalSpots(5)
                .providerId(55L)
                .active(Boolean.TRUE)
                .build();

        LocalDateTime now = LocalDateTime.now();
        ParkingReservationEntity occupiedReservation = reservationForWindow(
                1L,
                10L,
                "Old Port Garage",
                now.minusMinutes(30),
                2,
                18.5,
                ReservationStatus.ACTIVE
        );
        ParkingReservationEntity futureReservation = reservationForWindow(
                2L,
                10L,
                "Old Port Garage",
                now.plusHours(2),
                3,
                12.0,
                ReservationStatus.CONFIRMED
        );
        ParkingReservationEntity completedReservation = reservationForWindow(
                3L,
                20L,
                "Plateau Parking",
                now.minusHours(6),
                1,
                9.25,
                ReservationStatus.COMPLETED
        );
        ParkingReservationEntity cancelledReservation = reservationForWindow(
                4L,
                20L,
                "Plateau Parking",
                now.plusHours(1),
                1,
                25.0,
                ReservationStatus.CANCELLED
        );

        when(parkingFacilityRepository.findByProviderIdAndActiveTrue(55L))
                .thenReturn(List.of(facilityOne, facilityTwo));
        when(parkingReservationRepository.findByFacilityIdInOrderByCreatedAtDesc(List.of(10L, 20L)))
                .thenReturn(List.of(occupiedReservation, futureReservation, completedReservation, cancelledReservation));

        CityParkingAnalyticsResponse response = parkingFacilityService.getProviderAnalytics(55L);

        assertEquals(2, response.totalFacilities());
        assertEquals(15, response.totalSpots());
        assertEquals(2, response.reservedSpaces());
        assertEquals(1, response.occupiedSpaces());
        assertEquals(13, response.availableSpots());
        assertEquals(39.75, response.totalRevenue(), 0.0001);
        assertEquals(2, response.facilities().size());
        assertEquals(1, response.activeReservations().size());
        assertEquals("Old Port Garage", response.activeReservations().get(0).facilityName());

        var oldPortMetric = response.facilities().stream()
                .filter(metric -> metric.facilityId().equals(10L))
                .findFirst()
                .orElseThrow();
        assertEquals(2, oldPortMetric.reservedSpaces());
        assertEquals(1, oldPortMetric.occupiedSpaces());
        assertEquals(8, oldPortMetric.availableSpots());
        assertEquals(30.5, oldPortMetric.totalRevenue(), 0.0001);
    }

    @Test
    void getProviderAnalytics_returnsEmptyAnalyticsWhenProviderHasNoFacilities() {
        when(parkingFacilityRepository.findByProviderIdAndActiveTrue(88L)).thenReturn(List.of());

        CityParkingAnalyticsResponse response = parkingFacilityService.getProviderAnalytics(88L);

        assertEquals(0, response.totalFacilities());
        assertEquals(0, response.totalSpots());
        assertEquals(0, response.reservedSpaces());
        assertEquals(0, response.occupiedSpaces());
        assertEquals(0, response.availableSpots());
        assertEquals(0.0, response.totalRevenue(), 0.0);
        assertTrue(response.facilities().isEmpty());
        assertTrue(response.activeReservations().isEmpty());
    }

    private ParkingReservationEntity reservationForWindow(
            Long reservationId,
            Long facilityId,
            String facilityName,
            LocalDateTime start,
            int durationHours,
            double totalCost,
            ReservationStatus status) {
        return ParkingReservationEntity.builder()
                .id(reservationId)
                .facilityId(facilityId)
                .facilityName(facilityName)
                .city("Montreal")
                .arrivalDate(start.toLocalDate().format(DATE_FORMATTER))
                .arrivalTime(start.toLocalTime().format(TIME_FORMATTER))
                .durationHours(durationHours)
                .totalCost(totalCost)
                .userId(999L)
                .status(status)
                .createdAt(start.minusHours(1))
                .build();
    }
}
