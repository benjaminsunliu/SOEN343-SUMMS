package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class TripEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Long citizenId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
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
