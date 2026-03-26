package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(name = "idx_trips_reservation_id", columnList = "reservation_id"),
                @Index(name = "idx_trips_citizen_end_time", columnList = "citizen_id,end_time"),
                @Index(name = "idx_trips_vehicle_end_time", columnList = "vehicle_id,end_time")
        }
)
public class TripEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "citizen_id", nullable = false)
    private Long citizenId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column
    private Long totalDurationMinutes;

    public TripEntity() {
    }

    public TripEntity(
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
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getReservationId() {
        return reservationId;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public void setCitizenId(Long citizenId) {
        this.citizenId = citizenId;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setTotalDurationMinutes(Long totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }
}
