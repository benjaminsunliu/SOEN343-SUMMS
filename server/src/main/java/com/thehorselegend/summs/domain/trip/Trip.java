package com.thehorselegend.summs.domain.trip;

import java.time.Duration;
import java.time.LocalDateTime;

public class Trip {

    private final Long id;
    private final Long reservationId;
    private final Long vehicleId;
    private final Long citizenId;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalDurationMinutes;
    private Double co2SavedKg;

    public Trip(
            Long id,
            Long reservationId,
            Long vehicleId,
            Long citizenId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long totalDurationMinutes) {
        this.id = id;
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.citizenId = citizenId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalDurationMinutes = totalDurationMinutes;
        this.co2SavedKg = 0.0;
    }

    public Trip(
            Long id,
            Long reservationId,
            Long vehicleId,
            Long citizenId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long totalDurationMinutes,
            Double co2SavedKg) {
        this.id = id;
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.citizenId = citizenId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalDurationMinutes = totalDurationMinutes;
        this.co2SavedKg = co2SavedKg;
    }

    public static Trip start(Long reservationId, Long vehicleId, Long citizenId, LocalDateTime startTime) {
        return new Trip(null, reservationId, vehicleId, citizenId, startTime, null, null);
    }

    public void complete(LocalDateTime completedAt) {
        if (!isActive()) {
            throw new IllegalStateException("Trip has already been ended.");
        }
        if (completedAt.isBefore(startTime)) {
            throw new IllegalArgumentException("Trip end time cannot be before start time.");
        }

        this.endTime = completedAt;
        this.totalDurationMinutes = Math.max(0L, Duration.between(startTime, completedAt).toMinutes());
    }

    public boolean isActive() {
        return endTime == null;
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getCitizenId() {
        return citizenId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Long getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public Double getCo2SavedKg() {
        return co2SavedKg;
    }
}