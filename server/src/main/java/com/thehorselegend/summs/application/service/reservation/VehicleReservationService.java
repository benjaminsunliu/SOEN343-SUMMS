package com.thehorselegend.summs.application.service.reservation;

import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import com.thehorselegend.summs.infrastructure.persistence.ReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.ReservationRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleMapper;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleReservationService extends AbstractReservationService<Vehicle> {

    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    public VehicleReservationService(VehicleRepository vehicleRepository,
                                     ReservationRepository reservationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    protected void validateAvailability(Vehicle vehicle, LocalDateTime start, LocalDateTime end) {
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is not available for reservation");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        // Optionally, check overlapping reservations in DB
        boolean hasConflict = reservationRepository.findByReservableId(vehicle.getId()).stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.PENDING
                        && start.isBefore(r.getEndDate())
                        && end.isAfter(r.getStartDate()));
        if (hasConflict) {
            throw new IllegalStateException("Vehicle already reserved in this period");
        }
    }

    @Override
    protected Reservation buildReservation(Vehicle vehicle, Long userId, LocalDateTime start, LocalDateTime end) {
        return new Reservation(
                null,
                userId,
                vehicle.getId(),
                start,
                end,
                "CITY",
                ReservationStatus.PENDING
        );
    }

    @Override
    @Transactional
    protected Reservation saveReservation(Reservation reservation) {
        var entity = ReservationMapper.toEntity(reservation);
        var savedEntity = reservationRepository.save(entity);
        vehicleRepository.findById(reservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .ifPresent(v -> {
                    v.reserve();
                    vehicleRepository.save(VehicleMapper.toEntity(v));
                });
        return ReservationMapper.toDomain(savedEntity);
    }

    @Transactional
    public Reservation reserveVehicle(
            Long userId,
            Long vehicleId,
            String city,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        Reservation reservation = createReservation(vehicle, userId, startDate, endDate);

        reservation.setCity(city);

        return saveReservation(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, Long userId) {

        var entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        var reservation = ReservationMapper.toDomain(entity);

        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalStateException("User not authorized to cancel this reservation");
        }

        reservation.cancel();

        vehicleRepository.findById(reservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .ifPresent(v -> {
                    v.makeAvailable();
                    vehicleRepository.save(VehicleMapper.toEntity(v));
                });

        var savedEntity = reservationRepository.save(ReservationMapper.toEntity(reservation));
        return ReservationMapper.toDomain(savedEntity);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(ReservationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId) {
        var entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        return ReservationMapper.toDomain(entity);
    }
}