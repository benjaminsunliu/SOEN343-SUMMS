package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_reservations")
@PrimaryKeyJoinColumn(name = "reservation_id")
public class ParkingReservationEntity extends ReservationEntity {

    @Column(name = "facility_name")
    private String facilityName;

    @Column(name = "facility_address")
    private String facilityAddress;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ParkingReservationEntity() {
        super();
    }

    public ParkingReservationEntity(
            Long id,
            Long userId,
            Long reservableId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String city,
            ReservationStatus status,
            String facilityName,
            String facilityAddress,
            Integer durationHours,
            Double totalCost,
            LocalDateTime createdAt
    ) {
        super(id, userId, reservableId, startDate, endDate, city, status);
        this.facilityName = facilityName;
        this.facilityAddress = facilityAddress;
        this.durationHours = durationHours;
        this.totalCost = totalCost;
        this.createdAt = createdAt;
    }

    @jakarta.persistence.PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (getStatus() == null) {
            setStatus(ReservationStatus.CONFIRMED);
        }
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityAddress() {
        return facilityAddress;
    }

    public void setFacilityAddress(String facilityAddress) {
        this.facilityAddress = facilityAddress;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}