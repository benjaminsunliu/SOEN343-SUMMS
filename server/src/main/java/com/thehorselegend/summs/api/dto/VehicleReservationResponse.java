package com.thehorselegend.summs.api.dto;

import com.thehorselegend.summs.domain.reservation.VehicleReservation;
import com.thehorselegend.summs.domain.vehicle.Location;

import java.time.LocalDateTime;

public class VehicleReservationResponse {

    private Long reservationId;
    private Long userId;
    private Long vehicleId;
    private String city;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Location startLocation;
    private Location endLocation;

    public VehicleReservationResponse() {}

    public VehicleReservationResponse(Long reservationId,
                                      Long userId,
                                      Long vehicleId,
                                      String city,
                                      String status,
                                      LocalDateTime startDate,
                                      LocalDateTime endDate,
                                      Location startLocation,
                                      Location endLocation) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.city = city;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public static VehicleReservationResponse fromDomain(VehicleReservation reservation) {
        return new VehicleReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getReservableId(),
                reservation.getCity(),
                reservation.getStatus().name(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStartLocation(),
                reservation.getEndLocation()
        );
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Location getStartLocation() { return startLocation; }
    public void setStartLocation(Location startLocation) { this.startLocation = startLocation; }

    public Location getEndLocation() { return endLocation; }
    public void setEndLocation(Location endLocation) { this.endLocation = endLocation; }
}
