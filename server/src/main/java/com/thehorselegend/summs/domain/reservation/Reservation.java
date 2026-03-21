package com.thehorselegend.summs.domain.reservation;

import com.thehorselegend.summs.infrastructure.persistence.LocationEmbeddable;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.VehicleEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude", nullable = false))
    })
    private LocationEmbeddable startLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_longitude", nullable = false))
    })
    private LocationEmbeddable endLocation;
    private String city;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.PENDING;

    public Long getReservationId() { return reservationId; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public VehicleEntity getVehicle() { return vehicle; }
    public void setVehicle(VehicleEntity vehicle) { this.vehicle = vehicle; }
    public LocationEmbeddable getStartLocation() { return startLocation; }
    public void setStartLocation(LocationEmbeddable startLocation) { this.startLocation = startLocation; }
    public LocationEmbeddable getEndLocation() { return endLocation; }
    public void setEndLocation(LocationEmbeddable endLocation) { this.endLocation = endLocation; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
