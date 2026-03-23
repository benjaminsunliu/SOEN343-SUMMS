package com.thehorselegend.summs.api.dto;

import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.vehicle.Vehicle;

public class VehicleReservationResponse {

    private Long reservationId;
    private Long userId;
    private Long vehicleId;
    private String city;
    private String status;

    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;

    // Default constructor
    public VehicleReservationResponse() {
    }

    // Full constructor
    public VehicleReservationResponse(Long reservationId, Long userId, Long vehicleId, String city, String status,
                                      Double startLatitude, Double startLongitude,
                                      Double endLatitude, Double endLongitude) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.city = city;
        this.status = status;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
    }

    // Factory method to build DTO from domain objects
    public static VehicleReservationResponse fromDomain(Reservation reservation, Vehicle vehicle) {
        VehicleReservationResponse response = new VehicleReservationResponse();
        response.reservationId = reservation.getId();
        response.userId = reservation.getUserId();
        response.vehicleId = reservation.getReservableId();
        response.city = reservation.getCity();
        response.status = reservation.getStatus().name();

        // vehicle-specific locations
        response.startLatitude = vehicle.getLocation().latitude();
        response.startLongitude = vehicle.getLocation().longitude();
        // for now, end location is same as start
        response.endLatitude = vehicle.getLocation().latitude();
        response.endLongitude = vehicle.getLocation().longitude();

        return response;
    }

    // Getters and setters
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }
}