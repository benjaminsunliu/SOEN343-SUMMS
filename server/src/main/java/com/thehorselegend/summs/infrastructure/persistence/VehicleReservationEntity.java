package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_reservations")
@PrimaryKeyJoinColumn(name = "reservation_id")
public class VehicleReservationEntity extends ReservationEntity {

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

    public VehicleReservationEntity() {
        super();
    }

    public VehicleReservationEntity(
            Long id,
            Long userId,
            Long reservableId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String city,
            ReservationStatus status,
            LocationEmbeddable startLocation,
            LocationEmbeddable endLocation
    ) {
        super(id, userId, reservableId, startDate, endDate, city, status);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public LocationEmbeddable getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LocationEmbeddable startLocation) {
        this.startLocation = startLocation;
    }

    public LocationEmbeddable getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LocationEmbeddable endLocation) {
        this.endLocation = endLocation;
    }
}