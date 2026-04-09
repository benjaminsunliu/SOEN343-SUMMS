package com.thehorselegend.summs.infrastructure.adapter;

import com.thehorselegend.summs.api.dto.ParkingFacilityDTO;
import com.thehorselegend.summs.api.dto.ParkingSearchRequestDTO;
import com.thehorselegend.summs.domain.parking.ParkingFacility;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.ParkingFacilityRepository;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
@Primary
public class DatabaseParkingAdapter implements IParkingService {
    private static final Set<ReservationStatus> OCCUPYING_STATUSES = Set.of(
            ReservationStatus.CONFIRMED,
            ReservationStatus.ACTIVE
    );

    private final ParkingFacilityRepository parkingFacilityRepository;
    private final ParkingReservationRepository parkingReservationRepository;

    public DatabaseParkingAdapter(ParkingFacilityRepository parkingFacilityRepository,
                                  ParkingReservationRepository parkingReservationRepository) {
        this.parkingFacilityRepository = parkingFacilityRepository;
        this.parkingReservationRepository = parkingReservationRepository;
    }

    @Override
    public List<ParkingFacilityDTO> searchFacilities(ParkingSearchRequestDTO request) {
        double maxPrice = request.getMaxPricePerHour() != null ? request.getMaxPricePerHour() : Double.MAX_VALUE;
        int duration = request.getDurationHours() != null && request.getDurationHours() > 0
                ? request.getDurationHours()
                : 1;

        List<ParkingFacility> candidates = resolveCandidates(request, maxPrice);

        return candidates.stream()
                .map(facility -> toDto(facility, duration, request))
                .sorted(Comparator.comparingDouble(dto -> dto.getDistanceKm() == null ? Double.MAX_VALUE : dto.getDistanceKm()))
                .toList();
    }

    private List<ParkingFacility> resolveCandidates(ParkingSearchRequestDTO request, double maxPrice) {
        if (request.getLatitude() != null
                && request.getLongitude() != null
                && request.getRadiusKm() != null
                && request.getRadiusKm() > 0) {
            return parkingFacilityRepository.findNearby(
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getRadiusKm(),
                    maxPrice);
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {
            return parkingFacilityRepository.findByCityIgnoreCaseAndPricePerHourLessThanEqualAndActiveTrue(request.getCity().trim(), maxPrice);
        }

        return parkingFacilityRepository.findByActiveTrue().stream()
                .filter(facility -> facility.getPricePerHour() != null && facility.getPricePerHour() <= maxPrice)
                .toList();
    }

    private ParkingFacilityDTO toDto(ParkingFacility facility, int duration, ParkingSearchRequestDTO request) {
        int reserved = parkingReservationRepository.countByFacilityIdAndStatusIn(
                facility.getFacilityId(),
                OCCUPYING_STATUSES);

        int total = facility.getTotalSpots() == null ? 0 : Math.max(0, facility.getTotalSpots());
        int available = Math.max(0, total - reserved);

        String availabilityStatus = available == 0
                ? "FULL"
                : ((double) available / Math.max(total, 1) <= 0.1 ? "ALMOST_FULL" : "AVAILABLE");

        double distanceKm = 0.0;
        if (request.getLatitude() != null && request.getLongitude() != null
                && facility.getLatitude() != null && facility.getLongitude() != null) {
            distanceKm = haversineDistanceKm(
                    request.getLatitude(),
                    request.getLongitude(),
                    facility.getLatitude(),
                    facility.getLongitude());
        }

        double pricePerHour = facility.getPricePerHour() == null ? 0.0 : facility.getPricePerHour();

        return ParkingFacilityDTO.builder()
                .facilityId(facility.getFacilityId())
                .name(facility.getName())
                .address(facility.getAddress())
                .city(facility.getCity())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .distanceKm(distanceKm)
                .pricePerHour(pricePerHour)
                .estimatedTotal(roundTwoDecimals(pricePerHour * duration))
                .rating(facility.getRating() == null ? 4.0 : facility.getRating())
                .availableSpots(available)
                .totalSpots(total)
                .availabilityStatus(availabilityStatus)
                .covered(Boolean.TRUE.equals(facility.getCovered()))
                .openTwentyFourHours(Boolean.TRUE.equals(facility.getOpenTwentyFourHours()))
                .evCharging(Boolean.TRUE.equals(facility.getEvCharging()))
                .security(Boolean.TRUE.equals(facility.getSecurity()))
                .amenityTags(buildAmenityTags(facility))
                .build();
    }

    private List<String> buildAmenityTags(ParkingFacility facility) {
        List<String> tags = new ArrayList<>();
        if (Boolean.TRUE.equals(facility.getCovered())) {
            tags.add("Covered");
        }
        if (Boolean.TRUE.equals(facility.getOpenTwentyFourHours())) {
            tags.add("24h");
        }
        if (Boolean.TRUE.equals(facility.getEvCharging())) {
            tags.add("EV Charging");
        }
        if (Boolean.TRUE.equals(facility.getSecurity())) {
            tags.add("Security");
        }
        return tags;
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
