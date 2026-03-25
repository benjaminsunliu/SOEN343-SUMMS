package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @Column(name = "reservation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long reservableId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude"))
    })
    private LocationEmbeddable startLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_longitude"))
    })
    private LocationEmbeddable endLocation;

    public ReservationEntity() {
    }

    public ReservationEntity(
            Long id,
            Long userId,
            Long reservableId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String city,
            ReservationStatus status,
            LocationEmbeddable startLocation,
            LocationEmbeddable endLocation) {
        this.id = id;
        this.userId = userId;
        this.reservableId = reservableId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.city = city;
        this.status = status;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReservableId() {
        return reservableId;
    }

    public void setReservableId(Long reservableId) {
        this.reservableId = reservableId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocationEmbeddable getStartLocation() { return startLocation; }
    public void setStartLocation(LocationEmbeddable startLocation) { this.startLocation = startLocation; }

    public LocationEmbeddable getEndLocation() { return endLocation; }
    public void setEndLocation(LocationEmbeddable endLocation) { this.endLocation = endLocation; }
}