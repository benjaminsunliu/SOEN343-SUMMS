package com.thehorselegend.summs.domain.reservation;

import java.time.LocalDateTime;

public abstract class Reservation {
    private final Long reservationId;
    private final Long userId;
    private ReservableType reservableType;
    private final Long reservableId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private String city;
    private ReservationStatus status;

    public Reservation(Long id, Long userId, Long reservableId, LocalDateTime startDate, LocalDateTime endDate, String city, ReservationStatus status) {
        this.reservationId = id;
        this.userId = userId;
        this.reservableId = reservableId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.city = city;
        this.status = status;
    }

    public Reservation(Long userId, Long reservableId,
                       LocalDateTime startDate, LocalDateTime endDate,
                       String city, ReservationStatus status) {
        this(null, userId, reservableId, startDate, endDate, city, status);
    }

    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return !(end.isBefore(this.startDate) || start.isAfter(this.endDate));
    }

    public void confirm() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be confirmed");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void activate() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be activated");
        }
        this.status = ReservationStatus.ACTIVE;
    }

    public void complete() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Only active reservations can be completed");
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public void expire() {
        if (status != ReservationStatus.PENDING && status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only pending or confirmed reservations can be expired");
        }
        this.status = ReservationStatus.EXPIRED;
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }
        if (status == ReservationStatus.EXPIRED) {
            throw new IllegalStateException("Expired reservations cannot be cancelled");
        }
        if (status == ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Active reservations cannot be cancelled");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public Long getId() {
        return reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getReservableId() {
        return reservableId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public String getCity() {
        return city;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setCity(String city) {
        this.city = city;
    }


}
