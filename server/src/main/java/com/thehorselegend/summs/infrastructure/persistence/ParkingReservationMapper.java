package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ParkingReservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ParkingReservationMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ParkingReservationMapper() {
    }

    public static ParkingReservationEntity toEntity(ParkingReservation reservation) {
        if (reservation == null) {
            return null;
        }

        LocalDateTime startDate = reservation.getStartDate();
        int durationHours = resolveDurationHours(reservation);

        return ParkingReservationEntity.builder()
                .id(reservation.getId())
                .facilityId(reservation.getReservableId())
                .facilityName(reservation.getFacilityName())
                .facilityAddress(reservation.getFacilityAddress())
                .city(reservation.getCity())
                .arrivalDate(startDate.toLocalDate().format(DATE_FORMATTER))
                .arrivalTime(startDate.toLocalTime().format(TIME_FORMATTER))
                .durationHours(durationHours)
                .totalCost(reservation.getTotalCost())
                .userId(reservation.getUserId())
                .status(reservation.getStatus())
                .build();
    }

    public static ParkingReservation toDomain(ParkingReservationEntity entity) {
        if (entity == null) {
            return null;
        }

        LocalDateTime arrivalDateTime = parseArrivalDateTime(entity.getArrivalDate(), entity.getArrivalTime());
        int durationHours = entity.getDurationHours() == null || entity.getDurationHours() <= 0
                ? 1
                : entity.getDurationHours();
        LocalDateTime endDateTime = arrivalDateTime.plusHours(durationHours);
        ReservationStatus status = entity.getStatus() == null
                ? ReservationStatus.CONFIRMED
                : entity.getStatus();

        return new ParkingReservation(
                entity.getId(),
                entity.getUserId(),
                entity.getFacilityId(),
                arrivalDateTime,
                endDateTime,
                entity.getCity(),
                status,
                entity.getFacilityName(),
                entity.getFacilityAddress(),
                durationHours,
                entity.getTotalCost()
        );
    }

    private static int resolveDurationHours(ParkingReservation reservation) {
        if (reservation.getDurationHours() != null && reservation.getDurationHours() > 0) {
            return reservation.getDurationHours();
        }

        long hours = Duration.between(reservation.getStartDate(), reservation.getEndDate()).toHours();
        return hours > 0 ? (int) hours : 1;
    }

    private static LocalDateTime parseArrivalDateTime(String arrivalDate, String arrivalTime) {
        if (arrivalDate == null || arrivalTime == null) {
            throw new IllegalArgumentException("Parking reservation has invalid arrival date/time");
        }

        try {
            LocalDate date = LocalDate.parse(arrivalDate, DATE_FORMATTER);
            LocalTime time = LocalTime.parse(arrivalTime);
            return LocalDateTime.of(date, time);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Parking reservation has invalid arrival date/time", ex);
        }
    }
}
