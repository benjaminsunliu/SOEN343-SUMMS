package com.thehorselegend.summs.application.service.reservation;

import com.thehorselegend.summs.domain.reservation.Reservation;

import java.time.LocalDateTime;

public abstract class AbstractReservationService<T> {

    public Reservation createReservation(T reservable,
                                         Long userId,
                                         LocalDateTime start,
                                         LocalDateTime end) {

        validateAvailability(reservable, start, end);

        Reservation reservation = buildReservation(reservable, userId, start, end);

        return saveReservation(reservation);
    }

    protected abstract void validateAvailability(T reservable,
                                                 LocalDateTime start,
                                                 LocalDateTime end);

    protected abstract Reservation buildReservation(T reservable,
                                                    Long userId,
                                                    LocalDateTime start,
                                                    LocalDateTime end);

    // ✅ FIX: add return type
    protected abstract Reservation saveReservation(Reservation reservation);
}