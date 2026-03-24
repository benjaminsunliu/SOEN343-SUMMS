package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingReservationService {
    private final ParkingReservationRepository reservationRepository;

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
                .status("CONFIRMED")
                .build();

        ParkingReservationEntity saved = reservationRepository.save(entity);

        log.info("Parking reservation created id={}", saved.getId());

        return ParkingReservationResponse.builder()
                .reservationId(saved.getId())
                .facilityName(saved.getFacilityName())
                .facilityAddress(saved.getFacilityAddress())
                .arrivalDate(saved.getArrivalDate())
                .arrivalTime(saved.getArrivalTime())
                .durationHours(saved.getDurationHours())
                .totalCost(saved.getTotalCost())
                .status(saved.getStatus())
                .confirmedAt(saved.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }
}
