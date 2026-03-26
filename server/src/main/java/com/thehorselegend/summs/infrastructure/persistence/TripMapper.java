package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.trip.Trip;

public final class TripMapper {

    private TripMapper() {
    }

    public static TripEntity toEntity(Trip trip) {
        if (trip == null) {
            return null;
        }

        return new TripEntity(
                trip.getId(),
                trip.getReservationId(),
                trip.getVehicleId(),
                trip.getCitizenId(),
                trip.getStartTime(),
                trip.getEndTime(),
                trip.getTotalDurationMinutes()
        );
    }

    public static Trip toDomain(TripEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Trip(
                entity.getId(),
                entity.getReservationId(),
                entity.getVehicleId(),
                entity.getCitizenId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getTotalDurationMinutes()
        );
    }
}