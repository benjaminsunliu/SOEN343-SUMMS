package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transit_search_results", indexes = {
    @Index(name = "idx_transit_result_type", columnList = "transitType"),
    @Index(name = "idx_transit_result_line", columnList = "lineNumber")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitSearchResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transitType;
    private String lineNumber;
    private String lineName;

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    public void prePersist() {
        if (searchedAt == null) {
            searchedAt = LocalDateTime.now();
        }
    }
}
