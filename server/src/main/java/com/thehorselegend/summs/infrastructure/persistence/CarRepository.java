package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// JPA Repository that supports queries specific to cars.
public interface CarRepository extends JpaRepository<CarEntity, Long> {
    // Implement niche searches later
}
