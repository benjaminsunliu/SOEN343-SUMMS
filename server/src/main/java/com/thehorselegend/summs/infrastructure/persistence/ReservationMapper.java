package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;

public class ReservationMapper {

    private ReservationMapper() {}

    public static ReservationEntity toEntity(Reservation reservation) {
        if (reservation == null) return null;

        return new ReservationEntity(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getReservableId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getCity(),
                reservation.getStatus()
        );
    }

    public static Reservation toDomain(ReservationEntity entity) {
        if (entity == null) return null;

        return new Reservation(
                entity.getId(),
                entity.getUserId(),
                entity.getReservableId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCity(),
                entity.getStatus()
        );
    }
}