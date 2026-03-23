package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.EndTripRequest;
import com.thehorselegend.summs.api.dto.LocationDto;
import com.thehorselegend.summs.api.dto.StartTripRequest;
import com.thehorselegend.summs.api.dto.TripResponse;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.LocationEmbeddable;
import com.thehorselegend.summs.infrastructure.persistence.TripEntity;
import com.thehorselegend.summs.infrastructure.persistence.TripRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleEntity;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TripService {

    private static final double DOWNTOWN_MIN_LAT = 45.4900;
    private static final double DOWNTOWN_MAX_LAT = 45.5300;
    private static final double DOWNTOWN_MIN_LON = -73.5900;
    private static final double DOWNTOWN_MAX_LON = -73.5400;

    private static final double VERDUN_MIN_LAT = 45.4400;
    private static final double VERDUN_MAX_LAT = 45.4700;
    private static final double VERDUN_MIN_LON = -73.6000;
    private static final double VERDUN_MAX_LON = -73.5500;

    private static final double PLATEAU_MIN_LAT = 45.5200;
    private static final double PLATEAU_MAX_LAT = 45.5500;
    private static final double PLATEAU_MIN_LON = -73.6100;
    private static final double PLATEAU_MAX_LON = -73.5600;

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;

    public TripService(TripRepository tripRepository, VehicleRepository vehicleRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public TripResponse startTrip(StartTripRequest request) {
        VehicleEntity vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + request.vehicleId()));

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalArgumentException("Vehicle is not available to start a trip.");
        }

        if (tripRepository.findByVehicleIdAndEndTimeIsNull(request.vehicleId()).isPresent()) {
            throw new IllegalArgumentException("Vehicle already has an active trip.");
        }

        TripEntity trip = new TripEntity(
                null,
                request.vehicleId(),
                request.citizenId(),
                LocalDateTime.now(),
                null,
                null
        );

        TripEntity savedTrip = tripRepository.save(trip);

        vehicle.setStatus(VehicleStatus.IN_USE);
        VehicleEntity updatedVehicle = vehicleRepository.save(vehicle);

        return toResponse(savedTrip, updatedVehicle.getStatus());
    }

    @Transactional
    public TripResponse endTrip(Long tripId, EndTripRequest request) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));

        if (trip.getEndTime() != null) {
            throw new IllegalArgumentException("Trip has already been ended.");
        }

        if (!isValidDropOffZone(request.dropOffLocation())) {
            throw new IllegalArgumentException("Trip cannot be ended outside a valid drop-off zone.");
        }

        VehicleEntity vehicle = vehicleRepository.findById(trip.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + trip.getVehicleId()));

        LocalDateTime endTime = LocalDateTime.now();
        long durationMinutes = Math.max(0L, Duration.between(trip.getStartTime(), endTime).toMinutes());

        trip.setEndTime(endTime);
        trip.setTotalDurationMinutes(durationMinutes);
        TripEntity savedTrip = tripRepository.save(trip);

        LocationDto dropOffLocation = request.dropOffLocation();
        vehicle.setLocation(new LocationEmbeddable(dropOffLocation.latitude(), dropOffLocation.longitude()));
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        VehicleEntity updatedVehicle = vehicleRepository.save(vehicle);

        return toResponse(savedTrip, updatedVehicle.getStatus());
    }

    public TripResponse getTripById(Long tripId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));

        VehicleEntity vehicle = vehicleRepository.findById(trip.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + trip.getVehicleId()));

        return toResponse(trip, vehicle.getStatus());
    }

    private boolean isValidDropOffZone(LocationDto location) {
        double latitude = location.latitude();
        double longitude = location.longitude();

        return isInsideZone(latitude, longitude, DOWNTOWN_MIN_LAT, DOWNTOWN_MAX_LAT, DOWNTOWN_MIN_LON, DOWNTOWN_MAX_LON)
                || isInsideZone(latitude, longitude, VERDUN_MIN_LAT, VERDUN_MAX_LAT, VERDUN_MIN_LON, VERDUN_MAX_LON)
                || isInsideZone(latitude, longitude, PLATEAU_MIN_LAT, PLATEAU_MAX_LAT, PLATEAU_MIN_LON, PLATEAU_MAX_LON);
    }

    private boolean isInsideZone(
            double latitude,
            double longitude,
            double minLat,
            double maxLat,
            double minLon,
            double maxLon) {
        return latitude >= minLat
                && latitude <= maxLat
                && longitude >= minLon
                && longitude <= maxLon;
    }

    private TripResponse toResponse(TripEntity trip, VehicleStatus vehicleStatus) {
        return new TripResponse(
                trip.getId(),
                trip.getVehicleId(),
                trip.getCitizenId(),
                trip.getStartTime(),
                trip.getEndTime(),
                trip.getTotalDurationMinutes(),
                vehicleStatus.name()
        );
    }
}
