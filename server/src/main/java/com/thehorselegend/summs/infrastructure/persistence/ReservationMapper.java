package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.vehicle.Location;

public final class ReservationMapper {

    private ReservationMapper() {
    }

    public static ReservationEntity toEntity(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        if (reservation instanceof VehicleReservation vehicleReservation) {
            return toVehicleEntity(vehicleReservation);
        }

        throw new IllegalArgumentException(
                "Unsupported reservation type: " + reservation.getClass().getSimpleName()
        );
    }

    public static Reservation toDomain(ReservationEntity entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof VehicleReservationEntity vehicleReservationEntity) {
            return toVehicleDomain(vehicleReservationEntity);
        }

        throw new IllegalArgumentException(
                "Unsupported reservation entity type: " + entity.getClass().getSimpleName()
        );
    }

    private static VehicleReservationEntity toVehicleEntity(VehicleReservation reservation) {
        LocationEmbeddable startEmbeddable = null;
        LocationEmbeddable endEmbeddable = null;

        if (reservation.getStartLocation() != null) {
            startEmbeddable = new LocationEmbeddable(
                    reservation.getStartLocation().latitude(),
                    reservation.getStartLocation().longitude()
            );
        }

        if (reservation.getEndLocation() != null) {
            endEmbeddable = new LocationEmbeddable(
                    reservation.getEndLocation().latitude(),
                    reservation.getEndLocation().longitude()
            );
        }

        return new VehicleReservationEntity(
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

    private static VehicleReservation toVehicleDomain(VehicleReservationEntity entity) {
        Location startLocation = null;
        Location endLocation = null;

        if (entity.getStartLocation() != null) {
            startLocation = new Location(
                    entity.getStartLocation().getLatitude(),
                    entity.getStartLocation().getLongitude()
            );
        }

        if (entity.getEndLocation() != null) {
            endLocation = new Location(
                    entity.getEndLocation().getLatitude(),
                    entity.getEndLocation().getLongitude()
            );
        }

        return new VehicleReservation(
                entity.getId(),
                entity.getUserId(),
                entity.getReservableId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCity(),
                entity.getStatus(),
                startLocation,
                endLocation
        );
    }
}