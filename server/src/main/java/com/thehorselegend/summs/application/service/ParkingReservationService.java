package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.domain.reservation.ParkingReservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingReservationService {
    private static final DateTimeFormatter CONFIRMED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ParkingReservationRepository reservationRepository;

    @Transactional
    public ParkingReservationResponse createReservation(
            CreateParkingReservationRequest request,
            Long userId) {

        log.info("Creating parking reservation for facilityId={} userId={}",
                request.getFacilityId(), userId);

        ParkingReservation reservation = buildParkingReservation(request, userId);
        reservation.confirm();

        ParkingReservationEntity saved = reservationRepository.save(
                ParkingReservationMapper.toEntity(reservation)
        );
        ParkingReservation savedReservation = ParkingReservationMapper.toDomain(saved);

        log.info("Parking reservation created id={}", saved.getId());

        return toResponse(savedReservation, saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ParkingReservationResponse> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(entity -> toResponse(ParkingReservationMapper.toDomain(entity), entity.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        ParkingReservationEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Parking reservation not found"));

        ParkingReservation parkingReservation = ParkingReservationMapper.toDomain(entity);

        if (!parkingReservation.getUserId().equals(userId)) {
            throw new IllegalStateException("User not authorized to cancel this parking reservation");
        }

        cancelDomainReservation(parkingReservation);

        entity.setStatus(parkingReservation.getStatus());
        reservationRepository.save(entity);
    }

    private ParkingReservation buildParkingReservation(CreateParkingReservationRequest request, Long userId) {
        if (request.getFacilityId() == null) {
            throw new IllegalArgumentException("Facility id is required");
        }

        Integer durationHours = request.getDurationHours();
        if (durationHours == null || durationHours <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 hour");
        }

        LocalDateTime startDate = parseArrivalDateTime(request.getArrivalDate(), request.getArrivalTime());

        return new ParkingReservation(
                userId,
                request.getFacilityId(),
                startDate,
                startDate.plusHours(durationHours),
                request.getCity(),
                request.getFacilityName(),
                request.getFacilityAddress(),
                durationHours,
                request.getTotalCost()
        );
    }

    private void cancelDomainReservation(ParkingReservation reservation) {
        try {
            reservation.cancel();
        } catch (IllegalStateException ex) {
            String message = ex.getMessage();
            if ("Reservation is already cancelled".equals(message)) {
                throw new IllegalStateException("Parking reservation is already cancelled");
            }
            if ("Expired reservations cannot be cancelled".equals(message)) {
                throw new IllegalStateException("Expired parking reservations cannot be cancelled");
            }
            if ("Active reservations cannot be cancelled".equals(message)) {
                throw new IllegalStateException("Parking reservation cannot be cancelled in its current state");
            }
            throw ex;
        }
    }

    private LocalDateTime parseArrivalDateTime(String arrivalDate, String arrivalTime) {
        if (arrivalDate == null || arrivalTime == null) {
            throw new IllegalArgumentException("Arrival date and time are required");
        }

        try {
            return LocalDateTime.of(
                    LocalDate.parse(arrivalDate),
                    LocalTime.parse(arrivalTime)
            );
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Invalid parking arrival date/time", ex);
        }
    }

    private ParkingReservationResponse toResponse(ParkingReservation reservation, LocalDateTime createdAt) {
        LocalDateTime confirmedAt = createdAt == null ? LocalDateTime.now() : createdAt;
        ReservationStatus status = reservation.getStatus() == null
                ? ReservationStatus.CONFIRMED
                : reservation.getStatus();

        return ParkingReservationResponse.builder()
                .reservationId(reservation.getId())
                .facilityName(reservation.getFacilityName())
                .facilityAddress(reservation.getFacilityAddress())
                .city(reservation.getCity())
                .arrivalDate(reservation.getStartDate().toLocalDate().toString())
                .arrivalTime(reservation.getStartDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .durationHours(reservation.getDurationHours())
                .totalCost(reservation.getTotalCost())
                .status(status.name())
                .confirmedAt(confirmedAt.format(CONFIRMED_AT_FORMATTER))
                .build();
    }
}
