package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ParkingReservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ParkingReservationMapper {

    private ParkingReservationMapper() {
    }

    public static ParkingReservationEntity toEntity(ParkingReservation reservation) {
        if (reservation == null) {
            return null;
        }

        LocalDateTime startDate = reservation.getStartDate();
        String arrivalDate = startDate != null ? startDate.toLocalDate().toString() : null;
        String arrivalTime = startDate != null ? startDate.toLocalTime().toString() : null;

        return ParkingReservationEntity.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .facilityId(reservation.getReservableId())
                .facilityName(reservation.getFacilityName())
                .facilityAddress(reservation.getFacilityAddress())
                .city(reservation.getCity())
                .arrivalDate(arrivalDate)
                .arrivalTime(arrivalTime)
                .durationHours(reservation.getDurationHours())
                .totalCost(reservation.getTotalCost())
                .status(reservation.getStatus())
                .createdAt(null)
                .build();
    }

    public static ParkingReservation toDomain(ParkingReservationEntity entity) {
        if (entity == null) {
            return null;
        }

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (entity.getArrivalDate() != null && entity.getArrivalTime() != null) {
            try {
                startDate = LocalDateTime.of(
                        LocalDate.parse(entity.getArrivalDate()),
                        LocalTime.parse(entity.getArrivalTime())
                );
                int durationHours = entity.getDurationHours() != null ? entity.getDurationHours() : 0;
                endDate = startDate.plusHours(durationHours);
            } catch (Exception ex) {
                // If date parsing fails, leave as null
            }
        }

        return new ParkingReservation(
                entity.getId(),
                entity.getUserId(),
                entity.getFacilityId(),
                startDate,
                endDate,
                entity.getCity(),
                entity.getStatus(),
                entity.getFacilityName(),
                entity.getFacilityAddress(),
                entity.getDurationHours(),
                entity.getTotalCost()
        );
    }
}
