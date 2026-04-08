package com.thehorselegend.summs.domain.parking;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "parking_facilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingFacility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facilityId;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;

    // Geo coordinates for distance calculation
    private Double latitude;
    private Double longitude;

    private Double pricePerHour;
    private Double rating;
    private Integer totalSpots;

    private Long providerId;

    @Builder.Default
    private Boolean active = Boolean.TRUE;

    // Amenity flags
    private Boolean covered;
    private Boolean openTwentyFourHours;
    private Boolean evCharging;
    private Boolean security;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingSpot> spots;

    // Count how many spots have AVAILABLE status
    @Transient
    public int getAvailableSpotCount() {
        if (spots == null) return 0;
        return (int) spots.stream()
                .filter(s -> s.getStatus().name().equals("AVAILABLE"))
                .count();
    }
}
