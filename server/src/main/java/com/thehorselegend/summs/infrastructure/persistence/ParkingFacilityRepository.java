package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.parking.ParkingFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingFacilityRepository extends JpaRepository<ParkingFacility, Long> {
     List<ParkingFacility> findByCity(String city);

    List<ParkingFacility> findByCityAndPricePerHourLessThanEqual(
            String city, Double maxPrice);

    @Query(value = """
        SELECT * FROM parking_facilities f
        WHERE (6371 * acos(
            cos(radians(:lat)) * cos(radians(f.latitude))
            * cos(radians(f.longitude) - radians(:lng))
            + sin(radians(:lat)) * sin(radians(f.latitude))
        )) < :radiusKm
        AND f.price_per_hour <= :maxPrice
        ORDER BY f.price_per_hour ASC
        """, nativeQuery = true)
    List<ParkingFacility> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("maxPrice") double maxPrice);
}
