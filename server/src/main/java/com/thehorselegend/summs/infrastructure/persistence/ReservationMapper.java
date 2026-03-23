package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.vehicle.Location;

public class ReservationMapper {

    private ReservationMapper() {}

    // Convert domain to entity
    public static ReservationEntity toEntity(Reservation reservation) {
        if (reservation == null) return null;

        Double startLat = null, startLon = null, endLat = null, endLon = null;

        if (reservation instanceof VehicleReservation vehicleReservation) {
            startLat = vehicleReservation.getStartLocation().latitude();
            startLon = vehicleReservation.getStartLocation().longitude();
            endLat = vehicleReservation.getEndLocation().latitude();
            endLon = vehicleReservation.getEndLocation().longitude();
        }

        return new ReservationEntity(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getReservableId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getCity(),
                reservation.getStatus(),
                startLat != null ? startLat.doubleValue() : null,
                startLon != null ? startLon.doubleValue() : null,
                endLat != null ? endLat.doubleValue() : null,
                endLon != null ? endLon.doubleValue() : null
        );
    }

    // Convert entity to domain
    public static Reservation toDomain(ReservationEntity entity) {
        if (entity == null) return null;

        // If entity has vehicle coordinates, create VehicleReservation
        if (entity.getStartLatitude() != null && entity.getStartLongitude() != null
                && entity.getEndLatitude() != null && entity.getEndLongitude() != null) {

            return new VehicleReservation(
                    entity.getId(),
                    entity.getUserId(),
                    entity.getReservableId(),
                    entity.getStartDate(),
                    entity.getEndDate(),
                    entity.getCity(),
                    new Location(entity.getStartLatitude(), entity.getStartLongitude()),
                    new Location(entity.getEndLatitude(), entity.getEndLongitude())
            );
        }

        // TODO: handle other reservation types here
        throw new IllegalArgumentException("Unknown reservation type for entity ID " + entity.getId());
    }
}