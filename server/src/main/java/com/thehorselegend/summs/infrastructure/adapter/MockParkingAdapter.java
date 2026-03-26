package com.thehorselegend.summs.infrastructure.adapter;

import com.thehorselegend.summs.api.dto.ParkingFacilityDTO;
import com.thehorselegend.summs.api.dto.ParkingSearchRequestDTO;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor  
public class MockParkingAdapter implements IParkingService {

    // Injected so we can subtract confirmed bookings from available spots
    private final ParkingReservationRepository reservationRepository;

    private static final List<ParkingFacilityDTO> MOCK_FACILITIES = List.of(
        ParkingFacilityDTO.builder()
            .facilityId(1L).name("Indigo Place des Arts")
            .address("175 rue Ste-Catherine O.").city("Montreal")
            .distanceKm(0.0).pricePerHour(3.50).rating(4.6)
            .availableSpots(47).totalSpots(200).availabilityStatus("AVAILABLE")
            .covered(true).openTwentyFourHours(true).evCharging(true).security(true)
            .amenityTags(List.of("Covered", "24h", "EV Charging", "Security")).build(),

        ParkingFacilityDTO.builder()
            .facilityId(2L).name("Indigo Ville Marie")
            .address("777 rue de la Gauchetiere O.").city("Montreal")
            .distanceKm(0.1).pricePerHour(4.00).rating(4.8)
            .availableSpots(4).totalSpots(300).availabilityStatus("ALMOST_FULL")
            .covered(true).openTwentyFourHours(true).evCharging(true).security(true)
            .amenityTags(List.of("Covered", "24h", "EV Charging", "Security")).build(),

        ParkingFacilityDTO.builder()
            .facilityId(3L).name("Parking Vieux-Montreal")
            .address("777 rue de la Gauchetiere O.").city("Montreal")
            .distanceKm(0.4).pricePerHour(2.75).rating(4.1)
            .availableSpots(23).totalSpots(100).availabilityStatus("AVAILABLE")
            .covered(false).openTwentyFourHours(true).evCharging(false).security(true)
            .amenityTags(List.of("24h", "Security")).build(),

        ParkingFacilityDTO.builder()
            .facilityId(4L).name("ABM Parking Centre-Eaton")
            .address("677 rue Ste-Catherine O.").city("Montreal")
            .distanceKm(0.3).pricePerHour(4.50).rating(4.8)
            .availableSpots(15).totalSpots(250).availabilityStatus("AVAILABLE")
            .covered(true).openTwentyFourHours(true).evCharging(true).security(true)
            .amenityTags(List.of("Covered", "24h", "EV Charging", "Security")).build(),

        ParkingFacilityDTO.builder()
            .facilityId(5L).name("BestPark Quartier Latin")
            .address("300 rue Ontario E.").city("Montreal")
            .distanceKm(0.8).pricePerHour(2.25).rating(3.9)
            .availableSpots(31).totalSpots(80).availabilityStatus("AVAILABLE")
            .covered(false).openTwentyFourHours(false).evCharging(false).security(false)
            .amenityTags(List.of()).build()
    );

    @Override
    public List<ParkingFacilityDTO> searchFacilities(ParkingSearchRequestDTO request) {
        double maxPrice = request.getMaxPricePerHour() != null
                ? request.getMaxPricePerHour() : 999.0;
        int duration = request.getDurationHours() != null
                ? request.getDurationHours() : 1;

        return MOCK_FACILITIES.stream()
                .filter(f -> f.getPricePerHour() <= maxPrice)
                .map(f -> {
                    // Count confirmed reservations for this facility and subtract
                    int booked = reservationRepository
                            .countByFacilityIdAndStatus(f.getFacilityId(), "CONFIRMED");
                    int realAvailable = Math.max(0, f.getAvailableSpots() - booked);

                    // Recalculate availability status based on real count
                    String status;
                    if (realAvailable == 0) {
                        status = "FULL";
                    } else if ((double) realAvailable / f.getTotalSpots() < 0.05) {
                        status = "ALMOST_FULL";
                    } else {
                        status = "AVAILABLE";
                    }

                    // Build a new DTO with updated values (don't mutate the static list)
                    return ParkingFacilityDTO.builder()
                            .facilityId(f.getFacilityId())
                            .name(f.getName())
                            .address(f.getAddress())
                            .city(f.getCity())
                            .distanceKm(f.getDistanceKm())
                            .pricePerHour(f.getPricePerHour())
                            .estimatedTotal(Math.round(
                                    f.getPricePerHour() * duration * 100.0) / 100.0)
                            .rating(f.getRating())
                            .availableSpots(realAvailable)
                            .totalSpots(f.getTotalSpots())
                            .availabilityStatus(status)
                            .covered(f.getCovered())
                            .openTwentyFourHours(f.getOpenTwentyFourHours())
                            .evCharging(f.getEvCharging())
                            .security(f.getSecurity())
                            .amenityTags(f.getAmenityTags())
                            .build();
                })
                .sorted(Comparator.comparingDouble(ParkingFacilityDTO::getDistanceKm))
                .collect(Collectors.toList());
    }
}