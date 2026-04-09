package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CityParkingActiveReservationDto;
import com.thehorselegend.summs.api.dto.CityParkingAnalyticsResponse;
import com.thehorselegend.summs.api.dto.CityParkingFacilityAnalyticsDto;
import com.thehorselegend.summs.api.dto.ParkingCatalogEntryDto;
import com.thehorselegend.summs.api.dto.ParkingFacilityDTO;
import com.thehorselegend.summs.api.dto.ParkingFacilityUpsertRequest;
import com.thehorselegend.summs.api.dto.ParkingSummaryDto;
import com.thehorselegend.summs.domain.parking.ParkingFacility;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.ParkingFacilityRepository;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ParkingFacilityService {
    private static final DateTimeFormatter ANALYTICS_CONFIRMED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Set<ReservationStatus> REVENUE_STATUSES = EnumSet.of(
            ReservationStatus.CONFIRMED,
            ReservationStatus.ACTIVE,
            ReservationStatus.COMPLETED,
            ReservationStatus.EXPIRED
    );
    private static final Set<ReservationStatus> RESERVED_STATUSES = EnumSet.of(
            ReservationStatus.CONFIRMED,
            ReservationStatus.ACTIVE
    );
    private static final Set<ReservationStatus> OCCUPIED_STATUSES = EnumSet.of(ReservationStatus.ACTIVE);

    private final ParkingFacilityRepository parkingFacilityRepository;
    private final ParkingReservationRepository parkingReservationRepository;

    public ParkingFacilityService(ParkingFacilityRepository parkingFacilityRepository,
                                  ParkingReservationRepository parkingReservationRepository) {
        this.parkingFacilityRepository = parkingFacilityRepository;
        this.parkingReservationRepository = parkingReservationRepository;
    }

    public List<ParkingFacilityDTO> getAllFacilities(Long providerId) {
        return parkingFacilityRepository.findByProviderIdAndActiveTrue(providerId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<ParkingFacilityDTO> getAllActiveFacilities() {
        return parkingFacilityRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .toList();
    }

    public List<ParkingCatalogEntryDto> getCatalogEntriesWithStatus(Long providerId) {
        Map<String, CsvFacilityAccumulator> catalog = readBundledCatalog();
        if (catalog.isEmpty()) {
            return List.of();
        }

        Map<String, ParkingFacility> existingByKey = buildExistingByKey(parkingFacilityRepository.findByProviderIdAndActiveTrue(providerId));
        List<ParkingCatalogEntryDto> entries = new ArrayList<>();

        for (CsvFacilityAccumulator value : catalog.values()) {
            ParkingFacility candidate = toFacility(value);
            ParkingFacility existing = existingByKey.get(facilityKey(candidate));

            entries.add(new ParkingCatalogEntryDto(
                    value.terrainCode,
                    candidate.getName(),
                    candidate.getAddress(),
                    candidate.getCity(),
                    candidate.getLatitude(),
                    candidate.getLongitude(),
                    candidate.getPricePerHour(),
                    candidate.getRating(),
                    candidate.getTotalSpots(),
                    Boolean.TRUE.equals(candidate.getCovered()),
                    Boolean.TRUE.equals(candidate.getOpenTwentyFourHours()),
                    Boolean.TRUE.equals(candidate.getEvCharging()),
                    Boolean.TRUE.equals(candidate.getSecurity()),
                    existing != null,
                    existing == null ? null : existing.getFacilityId()
            ));
        }

        return entries;
    }

    @Transactional
    public ParkingFacilityDTO addCatalogEntryByTerrainCode(String terrainCode, Long providerId) {
        if (terrainCode == null || terrainCode.isBlank()) {
            throw new IllegalArgumentException("Terrain code is required");
        }

        Map<String, CsvFacilityAccumulator> catalog = readBundledCatalog();
        CsvFacilityAccumulator selected = catalog.get(terrainCode.trim());
        if (selected == null) {
            throw new IllegalArgumentException("Catalog parking space not found for terrain code: " + terrainCode);
        }

        ParkingFacility candidate = toFacility(selected);
        ParkingFacility existing = parkingFacilityRepository.findByNameAndAddressAndCityIgnoreCaseAndProviderId(
                        candidate.getName(),
                        candidate.getAddress(),
                        candidate.getCity(),
                        providerId)
                .orElse(null);

        if (existing != null) {
            if (!Boolean.TRUE.equals(existing.getActive())) {
                existing.setActive(Boolean.TRUE);
                parkingFacilityRepository.save(existing);
            }
            return toDto(existing);
        }

        candidate.setProviderId(providerId);
        candidate.setActive(Boolean.TRUE);

        return toDto(parkingFacilityRepository.save(candidate));
    }

    @Transactional
    public ParkingFacilityDTO createFacility(ParkingFacilityUpsertRequest request, Long providerId) {
        ParkingFacility facility = buildEntity(new ParkingFacility(), request);
        facility.setProviderId(providerId);
        facility.setActive(Boolean.TRUE);
        return toDto(parkingFacilityRepository.save(facility));
    }

    @Transactional
    public ParkingFacilityDTO updateFacility(Long facilityId, ParkingFacilityUpsertRequest request, Long providerId) {
        ParkingFacility existing = parkingFacilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Parking facility not found with id: " + facilityId));

        ensureOwnedByProvider(existing, providerId);

        if (!Boolean.TRUE.equals(existing.getActive())) {
            throw new IllegalArgumentException("Parking facility is not active for this provider");
        }

        ParkingFacility updated = buildEntity(existing, request);
        return toDto(parkingFacilityRepository.save(updated));
    }

    @Transactional
    public void deleteFacility(Long facilityId, Long providerId) {
        ParkingFacility existing = parkingFacilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Parking facility not found with id: " + facilityId));

        ensureOwnedByProvider(existing, providerId);
        existing.setActive(Boolean.FALSE);
        parkingFacilityRepository.save(existing);
    }

    public ParkingSummaryDto getSummary() {
        List<ParkingFacility> facilities = parkingFacilityRepository.findByActiveTrue();
        int totalFacilities = facilities.size();
        int totalSpots = facilities.stream()
                .map(ParkingFacility::getTotalSpots)
                .filter(spots -> spots != null && spots > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int reservedSpots = facilities.stream()
                .map(ParkingFacility::getFacilityId)
                .filter(id -> id != null)
                .mapToInt(id -> parkingReservationRepository.countByFacilityIdAndStatusIn(id, RESERVED_STATUSES))
                .sum();

        int availableSpots = Math.max(0, totalSpots - reservedSpots);
        return new ParkingSummaryDto(totalFacilities, totalSpots, availableSpots, reservedSpots);
    }

    @Transactional(readOnly = true)
    public CityParkingAnalyticsResponse getProviderAnalytics(Long providerId) {
        List<ParkingFacility> facilities = parkingFacilityRepository.findByProviderIdAndActiveTrue(providerId);
        if (facilities.isEmpty()) {
            return new CityParkingAnalyticsResponse(0, 0, 0, 0, 0, 0.0, List.of(), List.of());
        }

        List<Long> facilityIds = facilities.stream()
                .map(ParkingFacility::getFacilityId)
                .filter(id -> id != null)
                .toList();

        List<ParkingReservationEntity> reservations = facilityIds.isEmpty()
                ? List.of()
                : parkingReservationRepository.findByFacilityIdInOrderByCreatedAtDesc(facilityIds);

        Map<Long, List<ParkingReservationEntity>> reservationsByFacility = new HashMap<>();
        for (ParkingReservationEntity reservation : reservations) {
            reservationsByFacility
                    .computeIfAbsent(reservation.getFacilityId(), ignored -> new ArrayList<>())
                    .add(reservation);
        }

        LocalDateTime now = LocalDateTime.now();
        List<CityParkingFacilityAnalyticsDto> facilityAnalytics = facilities.stream()
                .map(facility -> toFacilityAnalytics(
                        facility,
                        reservationsByFacility.getOrDefault(facility.getFacilityId(), List.of()),
                        now))
                .sorted(Comparator.comparing(CityParkingFacilityAnalyticsDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int totalSpots = facilityAnalytics.stream()
                .mapToInt(CityParkingFacilityAnalyticsDto::totalSpots)
                .sum();
        int reservedSpaces = facilityAnalytics.stream()
                .mapToInt(CityParkingFacilityAnalyticsDto::reservedSpaces)
                .sum();
        int occupiedSpaces = facilityAnalytics.stream()
                .mapToInt(CityParkingFacilityAnalyticsDto::occupiedSpaces)
                .sum();
        int availableSpots = facilityAnalytics.stream()
                .mapToInt(CityParkingFacilityAnalyticsDto::availableSpots)
                .sum();
        double totalRevenue = facilityAnalytics.stream()
                .mapToDouble(CityParkingFacilityAnalyticsDto::totalRevenue)
                .sum();

        List<CityParkingActiveReservationDto> activeReservations = reservations.stream()
                .filter(reservation -> isCurrentlyOccupied(reservation, now))
                .sorted(Comparator.comparing(this::resolveReservationStart, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toActiveReservationDto)
                .toList();

        return new CityParkingAnalyticsResponse(
                facilities.size(),
                totalSpots,
                reservedSpaces,
                occupiedSpaces,
                availableSpots,
                totalRevenue,
                facilityAnalytics,
                activeReservations
        );
    }

    @Transactional
    public void validateBundledCatalogPresence() {
        readBundledCatalog();
    }

    private Map<String, CsvFacilityAccumulator> readBundledCatalog() {
        ClassPathResource resource = new ClassPathResource("parking/BornesHorsRue.csv");
        if (!resource.exists()) {
            throw new IllegalStateException("Bundled parking CSV not found at classpath: parking/BornesHorsRue.csv");
        }

        Map<String, CsvFacilityAccumulator> byTerrain = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreSurroundingSpaces(true)
                     .get()
                     .parse(reader)) {

            List<String> headers = parser.getHeaderNames().stream()
                    .map(this::normalize)
                    .toList();

            for (CSVRecord row : parser) {
                String status = optionalString(row, headers, "sstatut", "status");
                if (status != null && !status.isBlank() && !"A".equalsIgnoreCase(status.trim())) {
                    continue;
                }

                String terrain = optionalString(row, headers, "sterrain", "terrain");
                if (terrain == null || terrain.isBlank()) {
                    continue;
                }

                Double latitude = optionalDouble(row, headers, "nlatitude", "latitude", "lat");
                Double longitude = optionalDouble(row, headers, "nlongitude", "longitude", "lon", "lng");
                if (latitude == null || longitude == null) {
                    continue;
                }

                String streetName = optionalString(row, headers, "snomrueprincipale", "name", "street");
                Double centsPerHour = optionalDouble(row, headers, "ntarifhoraire", "tarif", "price");

                CsvFacilityAccumulator accumulator = byTerrain.computeIfAbsent(
                        terrain.trim(),
                        key -> new CsvFacilityAccumulator(terrain.trim(), streetName, centsPerHour));

                accumulator.addCoordinate(latitude, longitude);
                accumulator.incrementSpots();
                accumulator.updatePricing(centsPerHour);
                accumulator.updateHours(
                        optionalString(row, headers, "dtheuredebutap"),
                        optionalString(row, headers, "dtheurefinap"));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load bundled parking CSV", ex);
        }

        return byTerrain;
    }

    private ParkingFacility buildEntity(ParkingFacility target, ParkingFacilityUpsertRequest request) {
        target.setName(request.getName().trim());
        target.setAddress(request.getAddress().trim());
        target.setCity(request.getCity().trim());
        target.setLatitude(request.getLatitude());
        target.setLongitude(request.getLongitude());
        target.setPricePerHour(request.getPricePerHour());
        target.setRating(request.getRating());
        target.setTotalSpots(request.getTotalSpots());
        target.setCovered(request.getCovered());
        target.setOpenTwentyFourHours(request.getOpenTwentyFourHours());
        target.setEvCharging(request.getEvCharging());
        target.setSecurity(request.getSecurity());
        return target;
    }

    private ParkingFacilityDTO toDto(ParkingFacility facility) {
        int reserved = parkingReservationRepository.countByFacilityIdAndStatusIn(
                facility.getFacilityId(),
                RESERVED_STATUSES);
        int total = facility.getTotalSpots() == null ? 0 : Math.max(facility.getTotalSpots(), 0);
        int available = Math.max(0, total - reserved);

        String availability = available == 0
                ? "FULL"
                : ((double) available / Math.max(total, 1) <= 0.1 ? "ALMOST_FULL" : "AVAILABLE");

        return ParkingFacilityDTO.builder()
                .facilityId(facility.getFacilityId())
                .name(facility.getName())
                .address(facility.getAddress())
                .city(facility.getCity())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .distanceKm(0.0)
                .pricePerHour(facility.getPricePerHour())
                .estimatedTotal(facility.getPricePerHour())
                .rating(facility.getRating())
                .availableSpots(available)
                .totalSpots(total)
                .availabilityStatus(availability)
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

    private CityParkingFacilityAnalyticsDto toFacilityAnalytics(
            ParkingFacility facility,
            List<ParkingReservationEntity> reservations,
            LocalDateTime now) {
        int totalSpots = facility.getTotalSpots() == null ? 0 : Math.max(facility.getTotalSpots(), 0);
        int reservedSpaces = (int) reservations.stream()
                .filter(reservation -> countsTowardReservedSpaces(reservation, now))
                .count();
        int occupiedSpaces = (int) reservations.stream()
                .filter(reservation -> isCurrentlyOccupied(reservation, now))
                .count();
        int availableSpots = Math.max(0, totalSpots - reservedSpaces);
        double totalRevenue = reservations.stream()
                .filter(this::countsTowardRevenue)
                .mapToDouble(reservation -> reservation.getTotalCost() == null ? 0.0 : reservation.getTotalCost())
                .sum();

        return new CityParkingFacilityAnalyticsDto(
                facility.getFacilityId(),
                facility.getName(),
                facility.getCity(),
                totalSpots,
                reservedSpaces,
                occupiedSpaces,
                availableSpots,
                totalRevenue
        );
    }

    private CityParkingActiveReservationDto toActiveReservationDto(ParkingReservationEntity reservation) {
        LocalDateTime confirmedAt = reservation.getCreatedAt() == null ? LocalDateTime.now() : reservation.getCreatedAt();
        String status = reservation.getStatus() == null ? ReservationStatus.CONFIRMED.name() : reservation.getStatus().name();

        return new CityParkingActiveReservationDto(
                reservation.getId(),
                reservation.getFacilityId(),
                reservation.getFacilityName(),
                reservation.getCity(),
                reservation.getArrivalDate(),
                reservation.getArrivalTime(),
                reservation.getDurationHours(),
                reservation.getTotalCost(),
                status,
                confirmedAt.format(ANALYTICS_CONFIRMED_AT_FORMATTER)
        );
    }

    private boolean countsTowardRevenue(ParkingReservationEntity reservation) {
        return reservation.getStatus() != null
                && REVENUE_STATUSES.contains(reservation.getStatus())
                && reservation.getTotalCost() != null;
    }

    private boolean countsTowardReservedSpaces(ParkingReservationEntity reservation, LocalDateTime now) {
        return reservation.getStatus() != null
                && RESERVED_STATUSES.contains(reservation.getStatus());
    }

    private boolean isCurrentlyOccupied(ParkingReservationEntity reservation, LocalDateTime now) {
        return reservation.getStatus() != null
                && OCCUPIED_STATUSES.contains(reservation.getStatus());
    }

    private LocalDateTime resolveReservationStart(ParkingReservationEntity reservation) {
        if (reservation.getArrivalDate() == null || reservation.getArrivalTime() == null) {
            return null;
        }

        try {
            return LocalDateTime.of(
                    LocalDate.parse(reservation.getArrivalDate()),
                    LocalTime.parse(reservation.getArrivalTime())
            );
        } catch (DateTimeException ex) {
            return null;
        }
    }

    private LocalDateTime resolveReservationEnd(ParkingReservationEntity reservation) {
        LocalDateTime start = resolveReservationStart(reservation);
        if (start == null) {
            return null;
        }

        int durationHours = reservation.getDurationHours() == null ? 0 : Math.max(reservation.getDurationHours(), 0);
        return start.plusHours(durationHours);
    }

    private Double optionalDouble(CSVRecord row, List<String> normalizedHeaders, String... candidates) {
        String value = value(row, normalizedHeaders, candidates);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String optionalString(CSVRecord row, List<String> normalizedHeaders, String... candidates) {
        String value = value(row, normalizedHeaders, candidates);
        return value == null ? null : value.trim();
    }

    private String value(CSVRecord row, List<String> normalizedHeaders, String... candidates) {
        for (String candidate : candidates) {
            String target = normalize(candidate);
            for (int i = 0; i < normalizedHeaders.size(); i++) {
                if (normalizedHeaders.get(i).equals(target) && i < row.size()) {
                    return row.get(i);
                }
            }
        }
        return null;
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private Map<String, ParkingFacility> buildExistingByKey(List<ParkingFacility> facilities) {
        Map<String, ParkingFacility> existing = new HashMap<>();
        for (ParkingFacility facility : facilities) {
            existing.put(facilityKey(facility), facility);
        }
        return existing;
    }

    private String facilityKey(ParkingFacility facility) {
        return normalize(facility.getName()) + "|"
                + normalize(facility.getAddress()) + "|"
                + normalize(facility.getCity());
    }

    private void ensureOwnedByProvider(ParkingFacility facility, Long providerId) {
        if (facility.getProviderId() == null || !facility.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("Parking facility does not belong to the current provider");
        }
    }

    private ParkingFacility toFacility(CsvFacilityAccumulator source) {
        ParkingFacility facility = new ParkingFacility();
        String streetLabel = source.streetName == null || source.streetName.isBlank()
                ? "Unknown"
                : source.streetName;

        facility.setName("Terrain " + source.terrainCode + " - " + streetLabel);
        facility.setAddress(streetLabel);
        facility.setCity("Montreal");
        facility.setLatitude(source.averageLatitude());
        facility.setLongitude(source.averageLongitude());
        facility.setPricePerHour(source.pricePerHour());
        facility.setRating(4.0);
        facility.setTotalSpots(Math.max(1, source.spotCount));
        facility.setCovered(false);
        facility.setOpenTwentyFourHours(source.openTwentyFourHours);
        facility.setEvCharging(false);
        facility.setSecurity(false);
        return facility;
    }

    private static final class CsvFacilityAccumulator {
        private final String terrainCode;
        private final String streetName;
        private double latitudeSum = 0.0;
        private double longitudeSum = 0.0;
        private int coordinateCount = 0;
        private int spotCount = 0;
        private Double pricePerHour;
        private boolean openTwentyFourHours = true;

        private CsvFacilityAccumulator(String terrainCode, String streetName, Double centsPerHour) {
            this.terrainCode = terrainCode;
            this.streetName = streetName;
            updatePricing(centsPerHour);
        }

        private void addCoordinate(double latitude, double longitude) {
            this.latitudeSum += latitude;
            this.longitudeSum += longitude;
            this.coordinateCount += 1;
        }

        private void incrementSpots() {
            this.spotCount += 1;
        }

        private void updatePricing(Double centsPerHour) {
            if (centsPerHour != null && centsPerHour >= 0) {
                this.pricePerHour = centsPerHour / 100.0;
            }
        }

        private void updateHours(String start, String end) {
            if (!"00:00:00".equals(start) || !"23:59:59".equals(end)) {
                this.openTwentyFourHours = false;
            }
        }

        private double averageLatitude() {
            return coordinateCount == 0 ? 0.0 : latitudeSum / coordinateCount;
        }

        private double averageLongitude() {
            return coordinateCount == 0 ? 0.0 : longitudeSum / coordinateCount;
        }

        private double pricePerHour() {
            return pricePerHour == null ? 0.0 : pricePerHour;
        }
    }
}
