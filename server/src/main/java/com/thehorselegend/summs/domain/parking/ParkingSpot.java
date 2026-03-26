package com.thehorselegend.summs.domain.parking;

import com.thehorselegend.summs.domain.parking.SpotStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_spots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;

    private String spotLabel; 

    @Enumerated(EnumType.STRING)
    private SpotStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private ParkingFacility facility;

    private String vehicleType;
}
