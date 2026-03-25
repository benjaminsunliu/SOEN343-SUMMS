package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.EndTripRequest;
import com.thehorselegend.summs.api.dto.LocationDto;
import com.thehorselegend.summs.api.dto.StartTripRequest;
import com.thehorselegend.summs.api.dto.TripResponse;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.trip.Trip;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.ReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.ReservationRepository;
import com.thehorselegend.summs.infrastructure.persistence.TripMapper;
import com.thehorselegend.summs.infrastructure.persistence.TripRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleMapper;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import com.thehorselegend.summs.shared.time.SummsTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RentalLifecycleService {

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
    private static final double RESERVATION_DESTINATION_TOLERANCE = 0.0020;
    private static final String INVALID_DROPOFF_MESSAGE =
            "Trip cannot be ended outside a valid drop-off zone or your reserved destination.";

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    public RentalLifecycleService(
            TripRepository tripRepository,
            VehicleRepository vehicleRepository,
            ReservationRepository reservationRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public TripResponse startTrip(Long citizenId, StartTripRequest request) {
        VehicleReservation reservation = reservationRepository.findById(request.reservationId())
                .map(ReservationMapper::toDomain)
                .filter(VehicleReservation.class::isInstance)
                .map(VehicleReservation.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + request.reservationId()));

        validateReservation(reservation, citizenId);
        authorizePayment(request.paymentAuthorizationCode());

        Vehicle vehicle = vehicleRepository.findById(reservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + reservation.getReservableId()));

        if (tripRepository.findByVehicleIdAndEndTimeIsNull(vehicle.getId()).isPresent()) {
            throw new IllegalArgumentException("Vehicle already has an active trip.");
        }

        if (tripRepository.findByCitizenIdAndEndTimeIsNull(citizenId).isPresent()) {
            throw new IllegalArgumentException("Citizen already has an active trip.");
        }

        reservation.activate();
        reservationRepository.save(ReservationMapper.toEntity(reservation));

        vehicle.startTrip();
        vehicleRepository.save(VehicleMapper.toEntity(vehicle));

        Trip trip = Trip.start(reservation.getId(), vehicle.getId(), citizenId, SummsTime.now());
        Trip savedTrip = TripMapper.toDomain(tripRepository.save(TripMapper.toEntity(trip)));

        return toResponse(savedTrip, vehicle.getStatus());
    }

    @Transactional
    public TripResponse endTrip(Long citizenId, Long tripId, EndTripRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .map(TripMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));

        if (!trip.getCitizenId().equals(citizenId)) {
            throw new IllegalArgumentException("Trip does not belong to the authenticated citizen.");
        }

        VehicleReservation reservation = reservationRepository.findById(trip.getReservationId())
                .map(ReservationMapper::toDomain)
                .filter(VehicleReservation.class::isInstance)
                .map(VehicleReservation.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + trip.getReservationId()));

        LocationDto dropOffLocation = request.dropOffLocation();
        boolean endedInValidZone = isValidDropOffZone(dropOffLocation);

        if (!isValidDropOffLocation(dropOffLocation, reservation)) {
            throw new IllegalArgumentException(INVALID_DROPOFF_MESSAGE);
        }

        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + trip.getVehicleId()));

        Location actualDropOffLocation = new Location(dropOffLocation.latitude(), dropOffLocation.longitude());

        if (endedInValidZone) {
            reservation.setEndLocation(actualDropOffLocation);
        }
        trip.complete(SummsTime.now());
        reservation.complete();
        reservationRepository.save(ReservationMapper.toEntity(reservation));

        vehicle.release(actualDropOffLocation);

        Trip savedTrip = TripMapper.toDomain(tripRepository.save(TripMapper.toEntity(trip)));
        vehicleRepository.save(VehicleMapper.toEntity(vehicle));

        return toResponse(savedTrip, vehicle.getStatus());
    }

    public TripResponse getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .map(TripMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));

        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + trip.getVehicleId()));

        return toResponse(trip, vehicle.getStatus());
    }

    public TripResponse getTripByReservationId(Long citizenId, Long reservationId) {
        Trip trip = tripRepository.findByReservationId(reservationId)
                .map(TripMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found for reservation id: " + reservationId));

        if (!trip.getCitizenId().equals(citizenId)) {
            throw new IllegalArgumentException("Trip does not belong to the authenticated citizen.");
        }

        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + trip.getVehicleId()));

        return toResponse(trip, vehicle.getStatus());
    }

    public TripResponse getActiveTripForCitizen(Long citizenId) {
        Trip trip = tripRepository.findByCitizenIdAndEndTimeIsNull(citizenId)
                .map(TripMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("No active trip found for citizen id: " + citizenId));

        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + trip.getVehicleId()));

        return toResponse(trip, vehicle.getStatus());
    }

    private void validateReservation(VehicleReservation reservation, Long citizenId) {
        if (!reservation.getUserId().equals(citizenId)) {
            throw new IllegalArgumentException("Reservation does not belong to the authenticated citizen.");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Reservation must be confirmed before starting a trip.");
        }

        if (!reservation.getEndDate().isAfter(SummsTime.now())) {
            throw new IllegalArgumentException("Reservation is expired.");
        }
    }

    private void authorizePayment(String paymentAuthorizationCode) {
        if (!paymentAuthorizationCode.startsWith("PAY-")) {
            throw new IllegalArgumentException("Payment authorization failed.");
        }
    }

    private boolean isValidDropOffZone(LocationDto location) {
        double latitude = location.latitude();
        double longitude = location.longitude();

        return isInsideZone(latitude, longitude, DOWNTOWN_MIN_LAT, DOWNTOWN_MAX_LAT, DOWNTOWN_MIN_LON, DOWNTOWN_MAX_LON)
                || isInsideZone(latitude, longitude, VERDUN_MIN_LAT, VERDUN_MAX_LAT, VERDUN_MIN_LON, VERDUN_MAX_LON)
                || isInsideZone(latitude, longitude, PLATEAU_MIN_LAT, PLATEAU_MAX_LAT, PLATEAU_MIN_LON, PLATEAU_MAX_LON);
    }

    private boolean isValidDropOffLocation(LocationDto location, VehicleReservation reservation) {
        return isValidDropOffZone(location) || isNearReservationDestination(location, reservation.getEndLocation());
    }

    private boolean isNearReservationDestination(LocationDto location, Location reservationDestination) {
        return Math.abs(location.latitude() - reservationDestination.latitude()) <= RESERVATION_DESTINATION_TOLERANCE
                && Math.abs(location.longitude() - reservationDestination.longitude()) <= RESERVATION_DESTINATION_TOLERANCE;
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

    private TripResponse toResponse(Trip trip, VehicleStatus vehicleStatus) {
        return new TripResponse(
                trip.getId(),
                trip.getReservationId(),
                trip.getVehicleId(),
                trip.getCitizenId(),
                trip.getStartTime(),
                trip.getEndTime(),
                trip.getTotalDurationMinutes(),
                vehicleStatus.name()
        );
    }
}
