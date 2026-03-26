package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingReservationService {
    private final ParkingReservationRepository reservationRepository;

    @Transactional
    public ParkingReservationResponse createReservation(
            CreateParkingReservationRequest request,
            Long userId) {

        log.info("Creating parking reservation for facilityId={} userId={}",
                request.getFacilityId(), userId);

        ParkingReservationEntity entity = ParkingReservationEntity.builder()
                .facilityId(request.getFacilityId())
                .facilityName(request.getFacilityName())
                .facilityAddress(request.getFacilityAddress())
                .city(request.getCity())
                .arrivalDate(request.getArrivalDate())
                .arrivalTime(request.getArrivalTime())
                .durationHours(request.getDurationHours())
                .totalCost(request.getTotalCost())
                .userId(userId)
                .status(ReservationStatus.CONFIRMED)
                .build();

        ParkingReservationEntity saved = reservationRepository.save(entity);

        log.info("Parking reservation created id={}", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ParkingReservationResponse> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        ParkingReservationEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Parking reservation not found"));

        if (!entity.getUserId().equals(userId)) {
            throw new IllegalStateException("User not authorized to cancel this parking reservation");
        }

        ReservationStatus currentStatus = entity.getStatus() == null
                ? ReservationStatus.CONFIRMED
                : entity.getStatus();
        if (currentStatus == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Parking reservation is already cancelled");
        }
        if (currentStatus == ReservationStatus.EXPIRED) {
            throw new IllegalStateException("Expired parking reservations cannot be cancelled");
        }
        if (currentStatus == ReservationStatus.ACTIVE || currentStatus == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Parking reservation cannot be cancelled in its current state");
        }

        entity.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(entity);
    }

    private ParkingReservationResponse toResponse(ParkingReservationEntity reservationEntity) {
        ReservationStatus status = reservationEntity.getStatus() == null
                ? ReservationStatus.CONFIRMED
                : reservationEntity.getStatus();

        return ParkingReservationResponse.builder()
                .reservationId(reservationEntity.getId())
                .facilityName(reservationEntity.getFacilityName())
                .facilityAddress(reservationEntity.getFacilityAddress())
                .city(reservationEntity.getCity())
                .arrivalDate(reservationEntity.getArrivalDate())
                .arrivalTime(reservationEntity.getArrivalTime())
                .durationHours(reservationEntity.getDurationHours())
                .totalCost(reservationEntity.getTotalCost())
                .status(status.name())
                .confirmedAt(reservationEntity.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }
}
