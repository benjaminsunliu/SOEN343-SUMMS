package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// JPA Repository that supports queries specific to scooters.
public interface ScooterRepository extends JpaRepository<ScooterEntity, Long> {
    // Implement niche searches later
}
