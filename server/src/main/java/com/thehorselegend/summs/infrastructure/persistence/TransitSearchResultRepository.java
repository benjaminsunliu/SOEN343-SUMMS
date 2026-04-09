package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransitSearchResultRepository extends JpaRepository<TransitSearchResultEntity, Long> {

    @Query("SELECT r.transitType, COUNT(r) as cnt FROM TransitSearchResultEntity r " +
           "WHERE r.transitType IS NOT NULL AND r.transitType <> '' " +
           "GROUP BY r.transitType ORDER BY cnt DESC")
    List<Object[]> findTopResultTransitTypes();

    @Query("SELECT r.lineNumber, r.lineName, COUNT(r) as cnt FROM TransitSearchResultEntity r " +
           "WHERE r.lineNumber IS NOT NULL AND r.lineNumber <> '' " +
           "GROUP BY r.lineNumber, r.lineName ORDER BY cnt DESC")
    List<Object[]> findTopResultLines();
}
