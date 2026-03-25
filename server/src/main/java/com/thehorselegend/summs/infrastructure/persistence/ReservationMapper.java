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

        LocationEmbeddable startEmbeddable = null;
        LocationEmbeddable endEmbeddable = null;

        if (reservation instanceof VehicleReservation vehicleReservation) {
            startEmbeddable = new LocationEmbeddable(
                    vehicleReservation.getStartLocation().latitude(),
                    vehicleReservation.getStartLocation().longitude()
            );
            endEmbeddable = new LocationEmbeddable(
                    vehicleReservation.getEndLocation().latitude(),
                    vehicleReservation.getEndLocation().longitude()
            );
        }

        return new ReservationEntity(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getReservableId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getCity(),
                reservation.getStatus(),
                startEmbeddable,
                endEmbeddable
        );
    }

    // Convert entity to domain
    public static Reservation toDomain(ReservationEntity entity) {
        if (entity == null) return null;

        // If entity has vehicle coordinates, create VehicleReservation
        if (entity.getStartLocation() != null && entity.getEndLocation() != null) {
            return new VehicleReservation(
                    entity.getId(),
                    entity.getUserId(),
                    entity.getReservableId(),
                    entity.getStartDate(),
                    entity.getEndDate(),
                    entity.getCity(),
                    entity.getStatus(),
                    new Location(entity.getStartLocation().getLatitude(), entity.getStartLocation().getLongitude()),
                    new Location(entity.getEndLocation().getLatitude(), entity.getEndLocation().getLongitude())
            );
        }

        throw new IllegalArgumentException("Unknown reservation type for entity ID " + entity.getId());
    }
}