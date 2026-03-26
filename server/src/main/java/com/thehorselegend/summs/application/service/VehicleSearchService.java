package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.ContextAwareVehicleResponse;
import com.thehorselegend.summs.api.dto.ContextAwareVehicleSearchResponse;
import com.thehorselegend.summs.api.dto.LocationDto;
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

    public ContextAwareVehicleSearchResponse searchVehicles(
            double userLat,
            double userLon,
            double radiusKm,
            String typeFilter
    ) {
        List<Vehicle> nearbyVehicles = findNearbyVehicles(userLat, userLon, radiusKm);
        List<Vehicle> filteredByType = applyTypeFilter(nearbyVehicles, typeFilter);
        WeatherCondition weather = resolveWeatherCondition(userLat, userLon);

        List<ContextAwareVehicleResponse> contextAwareVehicles = filteredByType.stream()
                .map(vehicle -> vehicleToContextAwareResponse(vehicle, weather))
                .collect(Collectors.toList());

        return new ContextAwareVehicleSearchResponse(
                weather.getType(),
                weather.getSeverity().name(),
                buildWeatherAdvisory(weather),
                contextAwareVehicles
        );
    }

    private boolean matchesType(Vehicle vehicle, String typeFilter) {
        switch (typeFilter.toLowerCase()) {
            case "car": return vehicle instanceof Car;
            case "scooter": return vehicle instanceof Scooter;
            case "bicycle": return vehicle instanceof Bicycle;
            default: return false;
        }
    }

    private List<Vehicle> applyTypeFilter(List<Vehicle> vehicles, String typeFilter) {
        if (typeFilter == null || typeFilter.isBlank()) {
            return vehicles;
        }

        return vehicles.stream()
                .filter(vehicle -> matchesType(vehicle, typeFilter))
                .collect(Collectors.toList());
    }

    private WeatherCondition resolveWeatherCondition(double userLat, double userLon) {
        try {
            return weatherService.getCurrentWeather(userLat, userLon);
        } catch (Exception ignored) {
            // Keep search available even if weather lookup fails.
            return new WeatherCondition("Unknown", Severity.LOW);
        }
    }

    private ContextAwareVehicleResponse vehicleToContextAwareResponse(
            Vehicle vehicle,
            WeatherCondition weather
    ) {
        LocationDto locationDto = locationToDto(vehicle.getLocation());
        String locationAddress = null;
        String locationCity = null;

        Double maxRange = null;
        String licensePlate = null;
        Integer seatingCapacity = null;

        String type;
        if (vehicle instanceof Car car) {
            type = VehicleType.CAR.name();
            licensePlate = car.getLicensePlate();
            seatingCapacity = car.getSeatingCapacity();
        } else if (vehicle instanceof Scooter scooter) {
            type = VehicleType.SCOOTER.name();
            maxRange = scooter.getMaxRange();
        } else if (vehicle instanceof Bicycle) {
            type = VehicleType.BICYCLE.name();
        } else {
            type = null;
        }

        boolean weatherRisky = isWeatherRisky(vehicle, weather);
        String weatherRiskMessage = weatherRisky ? buildVehicleRiskMessage(weather) : null;

        return new ContextAwareVehicleResponse(
                vehicle.getId(),
                type,
                vehicle.getStatus().name(),
                locationDto,
                locationAddress,
                locationCity,
                vehicle.getProviderId(),
                vehicle.getCostPerMinute(),
                maxRange,
                licensePlate,
                seatingCapacity,
                weatherRisky,
                weatherRiskMessage
        );
    }

    private boolean isWeatherRisky(Vehicle vehicle, WeatherCondition weather) {
        return weather.getSeverity() == Severity.HIGH && !(vehicle instanceof Car);
    }

    private String buildVehicleRiskMessage(WeatherCondition weather) {
        return "Risky due to " + weather.getType() + " (" + weather.getSeverity().name() + ") conditions.";
    }

    private String buildWeatherAdvisory(WeatherCondition weather) {
        if (weather.getSeverity() == Severity.HIGH) {
            return "Severe weather detected. You can still reserve any vehicle, but two-wheel vehicles are risky.";
        }
        if (weather.getSeverity() == Severity.MEDIUM) {
            return "Moderate weather conditions. Ride carefully.";
        }
        return "Weather conditions are currently favorable.";
    }

    private LocationDto locationToDto(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationDto(location.latitude(), location.longitude());
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
