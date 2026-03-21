package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// JPA Repository that supports queries specific to bicycles.
public interface BicycleRepository extends JpaRepository<BicycleEntity, Long> {
    // Implement niche searches later
}
