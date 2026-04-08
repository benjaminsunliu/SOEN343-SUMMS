package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transit_search_logs", indexes = {
    @Index(name = "idx_transit_origin",      columnList = "origin"),
    @Index(name = "idx_transit_destination",  columnList = "destination"),
    @Index(name = "idx_transit_searched_at",  columnList = "searchedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitSearchLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origin;
    private String destination;
    private String transitType;       // ALL, METRO, BUS, REM
    private String date;              // requested travel date
    private String time;              // requested departure time
    private Integer resultsReturned;  // how many routes came back
    private Long userId;              // who searched (nullable for anonymous)

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    public void prePersist() {
        if (searchedAt == null) searchedAt = LocalDateTime.now();
    }
}
