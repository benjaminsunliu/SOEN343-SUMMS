package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

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
    private String transitType;       // requested search type: ALL, METRO, BUS, REM
    private String date;              // requested travel date
    private String time;              // requested departure time

    @ElementCollection
    @CollectionTable(
        name = "transit_search_log_returned_types",
        joinColumns = @JoinColumn(name = "search_log_id"),
        indexes = @Index(name = "idx_transit_log_returned_type", columnList = "transit_type")
    )
    @Column(name = "transit_type")
    @Builder.Default
    private Set<String> returnedTransitTypes = new LinkedHashSet<>();

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    public void prePersist() {
        if (searchedAt == null) searchedAt = LocalDateTime.now();
    }
}
