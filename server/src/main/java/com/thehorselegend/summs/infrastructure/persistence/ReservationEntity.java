package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_reservations_user_id", columnList = "user_id"),
                @Index(name = "idx_reservations_reservable_id", columnList = "reservable_id"),
                @Index(name = "idx_reservations_status_end_date", columnList = "status,end_date")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "reservation_type", discriminatorType = DiscriminatorType.STRING)
public abstract class ReservationEntity {

    @Id
    @Column(name = "reservation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reservable_id", nullable = false)
    private Long reservableId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    protected ReservationEntity() {
    }

    protected ReservationEntity(
            Long id,
            Long userId,
            Long reservableId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String city,
            ReservationStatus status
    ) {
        this.id = id;
        this.userId = userId;
        this.reservableId = reservableId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.city = city;
        this.status = status;
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
}