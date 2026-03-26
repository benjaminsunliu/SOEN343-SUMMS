package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which facility was booked
    @Column(nullable = false)
    private Long facilityId;

    @Column(nullable = false)
    private String facilityName;

    private String facilityAddress;
    private String city;

    // Trip details
    private String  arrivalDate;
    private String  arrivalTime;
    private Integer durationHours;
    private Double  totalCost;

    // Who booked it — userId from auth
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null)    status = ReservationStatus.CONFIRMED;
    }
}
