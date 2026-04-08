package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ParkingReservation;

public final class ParkingReservationMapper {

    private ParkingReservationMapper() {
    }

    public static ParkingReservationEntity toEntity(ParkingReservation reservation) {
        if (reservation == null) {
            return null;
        }

        return new ParkingReservationEntity(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getReservableId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getCity(),
                reservation.getStatus(),
                reservation.getFacilityName(),
                reservation.getFacilityAddress(),
                reservation.getDurationHours(),
                reservation.getTotalCost(),
                null
        );
    }

    public static ParkingReservation toDomain(ParkingReservationEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ParkingReservation(
                entity.getId(),
                entity.getUserId(),
                entity.getReservableId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCity(),
                entity.getStatus(),
                entity.getFacilityName(),
                entity.getFacilityAddress(),
                entity.getDurationHours(),
                entity.getTotalCost()
        );
    }
}
