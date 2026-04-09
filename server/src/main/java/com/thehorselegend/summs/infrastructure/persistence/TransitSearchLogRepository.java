package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransitSearchLogRepository extends JpaRepository<TransitSearchLogEntity, Long>{
    long countByOrigin(String origin);
    long countByDestination(String destination);

    @Query("SELECT t.origin, COUNT(t) as cnt FROM TransitSearchLogEntity t " +
           "GROUP BY t.origin ORDER BY cnt DESC")
    List<Object[]> findTopOrigins();

    @Query("SELECT t.destination, COUNT(t) as cnt FROM TransitSearchLogEntity t " +
           "GROUP BY t.destination ORDER BY cnt DESC")
    List<Object[]> findTopDestinations();

    @Query("SELECT t.transitType, COUNT(t) as cnt FROM TransitSearchLogEntity t " +
           "GROUP BY t.transitType ORDER BY cnt DESC")
    List<Object[]> findSearchesByType();

    @Query("SELECT returnedTransitType, COUNT(t) FROM TransitSearchLogEntity t " +
           "JOIN t.returnedTransitTypes returnedTransitType " +
           "GROUP BY returnedTransitType ORDER BY COUNT(t) DESC")
    List<Object[]> findTopReturnedTransitTypes();
}
