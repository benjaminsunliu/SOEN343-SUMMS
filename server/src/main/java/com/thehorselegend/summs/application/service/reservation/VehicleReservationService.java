package com.thehorselegend.summs.application.service.reservation;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.vehicle.Location;
import com.thehorselegend.summs.domain.vehicle.Vehicle;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import com.thehorselegend.summs.infrastructure.persistence.ReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.ReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ReservationRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleEntity;
import com.thehorselegend.summs.infrastructure.persistence.VehicleMapper;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import com.thehorselegend.summs.shared.time.SummsTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class VehicleReservationService extends ReservationCreationTemplate<VehicleReservationService.VehicleReservationSource, VehicleReservation> {

    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    public VehicleReservationService(VehicleRepository vehicleRepository,
                                     ReservationRepository reservationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    protected void validateAvailability(
            VehicleReservationSource source,
            LocalDateTime start,
            LocalDateTime end) {
        Vehicle vehicle = source.vehicle();

        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is not available for reservation");
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        boolean hasConflict = reservationRepository.findByReservableId(vehicle.getId()).stream()
                .map(ReservationMapper::toDomain)
                .filter(reservation -> reservation instanceof VehicleReservation)
                .anyMatch(reservation ->
                        (reservation.getStatus() == ReservationStatus.PENDING
                                || reservation.getStatus() == ReservationStatus.CONFIRMED)
                                && start.isBefore(reservation.getEndDate())
                                && end.isAfter(reservation.getStartDate())
                );

        if (hasConflict) {
            throw new IllegalStateException("Vehicle already reserved in this period");
        }
    }

    @Override
    protected VehicleReservation buildReservation(
            VehicleReservationSource source,
            Long userId,
            LocalDateTime start,
            LocalDateTime end) {
        Vehicle vehicle = source.vehicle();

        VehicleReservation reservation = new VehicleReservation(
                userId,
                vehicle.getId(),
                start,
                end,
                source.city(),
                vehicle.getLocation(),
                source.endLocation()
        );

        reservation.confirm();
        return reservation;
    }

    @Override
    @Transactional
    protected VehicleReservation saveReservation(VehicleReservation reservation) {
        ReservationEntity entity = ReservationMapper.toEntity(reservation);
        ReservationEntity savedEntity = reservationRepository.save(entity);

        vehicleRepository.findById(reservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .ifPresent(vehicle -> {
                    vehicle.reserve();
                    vehicleRepository.save(VehicleMapper.toEntity(vehicle));
                });

        return (VehicleReservation) ReservationMapper.toDomain(savedEntity);
    }

    @Transactional
    public VehicleReservation reserveVehicle(
            Long userId,
            Long vehicleId,
            String city,
            Location endLocation,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .map(VehicleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        return createReservation(
                new VehicleReservationSource(vehicle, city, endLocation),
                userId,
                startDate,
                endDate
        );
    }

    @Transactional
    public VehicleReservation cancelReservation(Long reservationId, Long userId) {
        var entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        VehicleReservation reservation = toVehicleReservation(ReservationMapper.toDomain(entity));

        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalStateException("User not authorized to cancel this reservation");
        }

        reservation.cancel();

        vehicleRepository.findById(reservation.getReservableId())
                .map(VehicleMapper::toDomain)
                .ifPresent(vehicle -> {
                    vehicle.makeAvailable();
                    vehicleRepository.save(VehicleMapper.toEntity(vehicle));
                });

        ReservationEntity savedEntity = reservationRepository.save(ReservationMapper.toEntity(reservation));
        return toVehicleReservation(ReservationMapper.toDomain(savedEntity));
    }

    @Transactional
    public List<VehicleReservation> getUserReservations(Long userId) {
        LocalDateTime now = SummsTime.now();
        List<ReservationEntity> reservationEntities = reservationRepository.findByUserId(userId);

        Set<Long> reservableIdsToRelease = new LinkedHashSet<>();
        List<ReservationEntity> entitiesToUpdate = new ArrayList<>();

        for (ReservationEntity reservationEntity : reservationEntities) {
            if (!(ReservationMapper.toDomain(reservationEntity) instanceof VehicleReservation reservation)) {
                continue;
            }

            if (shouldExpire(reservation.getStatus(), reservation.getEndDate(), now)) {
                reservation.expire();
                entitiesToUpdate.add(ReservationMapper.toEntity(reservation));
                reservableIdsToRelease.add(reservation.getReservableId());
            }
        }

        if (!entitiesToUpdate.isEmpty()) {
            reservationRepository.saveAll(entitiesToUpdate);
        }

        releaseReservedVehicles(reservableIdsToRelease);

        return reservationRepository.findByUserId(userId).stream()
                .map(ReservationMapper::toDomain)
                .filter(reservation -> reservation instanceof VehicleReservation)
                .map(reservation -> (VehicleReservation) reservation)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleReservation getReservationById(Long reservationId) {
        ReservationEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        return toVehicleReservation(ReservationMapper.toDomain(entity));
    }

    @Transactional
    public VehicleReservation getUserReservationById(Long reservationId, Long userId) {
        VehicleReservation reservation = getReservationById(reservationId);
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalStateException("User not authorized to access this reservation");
        }
        return expireReservationIfNeeded(reservation, SummsTime.now());
    }

    private VehicleReservation expireReservationIfNeeded(VehicleReservation reservation, LocalDateTime now) {
        if (!shouldExpire(reservation.getStatus(), reservation.getEndDate(), now)) {
            return reservation;
        }

        reservation.expire();
        VehicleReservation expiredReservation = toVehicleReservation(
                ReservationMapper.toDomain(
                        reservationRepository.save(ReservationMapper.toEntity(reservation))
                )
        );

        releaseReservedVehicles(Set.of(reservation.getReservableId()));

        return expiredReservation;
    }

    private void releaseReservedVehicles(Set<Long> reservableIds) {
        if (reservableIds.isEmpty()) {
            return;
        }

        List<VehicleEntity> vehicles = vehicleRepository.findAllById(reservableIds);
        List<VehicleEntity> vehiclesToUpdate = new ArrayList<>();

        for (VehicleEntity vehicleEntity : vehicles) {
            if (vehicleEntity.getStatus() == VehicleStatus.RESERVED) {
                vehicleEntity.setStatus(VehicleStatus.AVAILABLE);
                vehiclesToUpdate.add(vehicleEntity);
            }
        }

        if (!vehiclesToUpdate.isEmpty()) {
            vehicleRepository.saveAll(vehiclesToUpdate);
        }
    }

    private boolean shouldExpire(ReservationStatus status, LocalDateTime endDate, LocalDateTime now) {
        boolean isExpirable = status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
        return isExpirable && !endDate.isAfter(now);
    }

    private VehicleReservation toVehicleReservation(com.thehorselegend.summs.domain.reservation.Reservation reservation) {
        if (reservation instanceof VehicleReservation vehicleReservation) {
            return vehicleReservation;
        }
        throw new IllegalArgumentException("Vehicle reservation not found");
    }

    static record VehicleReservationSource(
            Vehicle vehicle,
            String city,
            Location endLocation
    ) {
    }
}


