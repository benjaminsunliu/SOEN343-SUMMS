package com.thehorselegend.summs.application.service.reservation;

import com.thehorselegend.summs.domain.reservation.Reservation;

import java.time.LocalDateTime;

public abstract class ReservationCreationTemplate<T, R extends Reservation> {

    public final R createReservation(
            T source,
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        validateAvailability(source, startDate, endDate);
        R reservation = buildReservation(source, userId, startDate, endDate);
        return saveReservation(reservation);
    }

    protected abstract void validateAvailability(
            T source,
            LocalDateTime start,
            LocalDateTime end);

    protected abstract R buildReservation(
            T source,
            Long userId,
            LocalDateTime start,
            LocalDateTime end);

    protected abstract R saveReservation(R reservation);
}