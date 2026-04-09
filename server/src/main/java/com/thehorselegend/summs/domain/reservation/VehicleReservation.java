package com.thehorselegend.summs.domain.reservation;

import com.thehorselegend.summs.domain.vehicle.Location;
import java.time.LocalDateTime;

public class VehicleReservation extends Reservation {

    private Location startLocation;
    private Location endLocation;

    public VehicleReservation(
            Long reservationId,
            Long userId,
            Long reservableId,
            LocalDateTime start,
            LocalDateTime end,
            String city,
            ReservationStatus status,
            Location startLocation,
            Location endLocation) {
        super(reservationId, userId, reservableId, start, end, city, status);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public VehicleReservation(Long userId,
                              Long reservableId,
                              LocalDateTime start,
                              LocalDateTime end,
                              String city,
                              Location startLocation,
                              Location endLocation) {
        super(userId, reservableId, start, end, city, ReservationStatus.PENDING);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    @Override
    public ReservableType getReservableType() {
        return ReservableType.VEHICLE;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }
}