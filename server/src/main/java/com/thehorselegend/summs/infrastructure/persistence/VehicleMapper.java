package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.vehicle.*;

/**
 * Mapper for converting between domain Vehicle objects and JPA VehicleEntity objects.
 * Handles polymorphic mapping across all vehicle subtypes.
 * Also converts Location value objects to/from LocationEmbeddable.
 * See <ref> LocationEmbeddable </ref> for details on location mapping.
 */
public class VehicleMapper {

    private VehicleMapper() {
    }

    public static VehicleEntity toEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        LocationEmbeddable locationEmbeddable = toLocationEmbeddable(vehicle.getLocation());

        if (vehicle instanceof Bicycle bicycle) {
            return new BicycleEntity(
                    bicycle.getId(),
                    bicycle.getStatus(),
                    locationEmbeddable,
                    bicycle.getProviderId(),
                    bicycle.getCostPerMinute()
            );
        } else if (vehicle instanceof Scooter scooter) {
            return new ScooterEntity(
                    scooter.getId(),
                    scooter.getStatus(),
                    locationEmbeddable,
                    scooter.getProviderId(),
                    scooter.getCostPerMinute(),
                    scooter.getMaxRange()
            );
        } else if (vehicle instanceof Car car) {
            return new CarEntity(
                    car.getId(),
                    car.getStatus(),
                    locationEmbeddable,
                    car.getProviderId(),
                    car.getCostPerMinute(),
                    car.getLicensePlate(),
                    car.getSeatingCapacity()
            );
        }

        throw new IllegalArgumentException("Unknown vehicle type: " + vehicle.getClass().getName());
    }

    public static Vehicle toDomain(VehicleEntity entity) {
        if (entity == null) {
            return null;
        }

        Location location = toLocation(entity.getLocation());
        VehicleStatus status = entity.getStatus();

        if (entity instanceof BicycleEntity bicycle) {
            return new Bicycle(
                    bicycle.getId(),
                    bicycle.getStatus(),
                    location,
                    bicycle.getProviderId(),
                    bicycle.getCostPerMinute()
            );
        } else if (entity instanceof ScooterEntity scooter) {
            return new Scooter(
                    scooter.getId(),
                    scooter.getStatus(),
                    location,
                    scooter.getProviderId(),
                    scooter.getCostPerMinute(),
                    scooter.getMaxRange()
            );
        } else if (entity instanceof CarEntity car) {
            return new Car(
                    car.getId(),
                    car.getStatus(),
                    location,
                    car.getProviderId(),
                    car.getCostPerMinute(),
                    car.getLicensePlate(),
                    car.getSeatingCapacity()
            );
        }

        throw new IllegalArgumentException("Unknown vehicle entity type: " + entity.getClass().getName());
    }

    // Converts domain Location record to JPA LocationEmbeddable
    private static LocationEmbeddable toLocationEmbeddable(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationEmbeddable(location.latitude(), location.longitude());
    }

    // Converts JPA LocationEmbeddable to domain Location record.
    private static Location toLocation(LocationEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        return new Location(embeddable.getLatitude(), embeddable.getLongitude());
    }
}
