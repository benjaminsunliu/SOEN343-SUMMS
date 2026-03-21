package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.weather.Severity;
import com.thehorselegend.summs.api.weather.WeatherCondition;
import com.thehorselegend.summs.domain.vehicle.*;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleSearchService {

    private final VehicleRepository vehicleRepository;
    private final WeatherService weatherService;

    public VehicleSearchService(VehicleRepository vehicleRepository, WeatherService weatherService) {
        this.vehicleRepository = vehicleRepository;
        this.weatherService = weatherService;
    }

    public List<Vehicle> findNearbyVehicles(double userLat, double userLon, double radiusKm) {
        List<VehicleEntity> availableEntities = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);

        return availableEntities.stream()
                .filter(v -> distance(userLat, userLon,
                        v.getLocation().getLatitude(),
                        v.getLocation().getLongitude()) <= radiusKm)
                .map(this::mapToConcreteVehicle)
                .collect(Collectors.toList());
    }

    public List<Vehicle> searchVehicles(double userLat, double userLon, double radiusKm, String typeFilter) {
        List<Vehicle> nearbyVehicles = findNearbyVehicles(userLat, userLon, radiusKm);

        if (nearbyVehicles.isEmpty()) {
            return nearbyVehicles;
        }

        WeatherCondition weather = weatherService.getCurrentWeather(userLat, userLon);
//        WeatherCondition weather = new WeatherCondition("Rain", Severity.HIGH); // Test

        List<Vehicle> filteredByWeather;

        if (weather.getSeverity() == Severity.HIGH) {
            filteredByWeather = nearbyVehicles.stream()
                    .filter(v -> v instanceof Car)
                    .collect(Collectors.toList());
        } else {
            filteredByWeather = nearbyVehicles;
        }

        if (typeFilter != null && !typeFilter.isEmpty()) {
            filteredByWeather = filteredByWeather.stream()
                    .filter(v -> matchesType(v, typeFilter))
                    .collect(Collectors.toList());
        }

        return filteredByWeather;
    }

    private boolean matchesType(Vehicle vehicle, String typeFilter) {
        switch (typeFilter.toLowerCase()) {
            case "car": return vehicle instanceof Car;
            case "scooter": return vehicle instanceof Scooter;
            case "bicycle": return vehicle instanceof Bicycle;
            default: return false;
        }
    }

    private Vehicle mapToConcreteVehicle(VehicleEntity entity) {
        Location loc = new Location(
                entity.getLocation().getLatitude(),
                entity.getLocation().getLongitude()
        );

        if (entity instanceof CarEntity carEntity) {
            return new Car(
                    carEntity.getId(),
                    carEntity.getStatus(),
                    loc,
                    carEntity.getProviderId(),
                    carEntity.getCostPerMinute(),
                    carEntity.getLicensePlate(),
                    4
            );
        } else if (entity instanceof ScooterEntity scooterEntity) {
            return new Scooter(
                    scooterEntity.getId(),
                    scooterEntity.getStatus(),
                    loc,
                    scooterEntity.getProviderId(),
                    scooterEntity.getCostPerMinute(),
                    scooterEntity.getMaxRange()
            );
        } else if (entity instanceof BicycleEntity bicycleEntity) {
            return new Bicycle(
                    bicycleEntity.getId(),
                    bicycleEntity.getStatus(),
                    loc,
                    bicycleEntity.getProviderId(),
                    bicycleEntity.getCostPerMinute()
            );
        } else {
            throw new IllegalArgumentException("Unknown vehicle type: " + entity.getClass().getSimpleName());
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}