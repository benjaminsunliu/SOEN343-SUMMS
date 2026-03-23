package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByUserId(Long userId);

    List<ReservationEntity> findByReservableId(Long reservableId);

    List<ReservationEntity> findByStatus(ReservationStatus status);
}