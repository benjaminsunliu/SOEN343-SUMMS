package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.*;
import com.thehorselegend.summs.domain.vehicle.*;
import com.thehorselegend.summs.infrastructure.persistence.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for vehicle operations.
 * Orchestrates business logic, domain objects, and persistence.
 * 
 * Features:
 * - Takes DTOs as input from API layer
 * - Uses domain factory for type-safe object creation
 * - Maps between domain and entity layers
 * - Delegates to repositories for persistence
 * - Returns DTOs to API layer
 * - Validates business rules
 * 
 * NOTE: Currently takes in <subclass>Repository but doesn't use them
 * Keeping them here (for now) in case we need to 
 * implement subtype-specific queries later.
 */
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BicycleRepository bicycleRepository;
    private final ScooterRepository scooterRepository;
    private final CarRepository carRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            BicycleRepository bicycleRepository,
            ScooterRepository scooterRepository,
            CarRepository carRepository) {
        this.vehicleRepository = vehicleRepository;
        this.bicycleRepository = bicycleRepository;
        this.scooterRepository = scooterRepository;
        this.carRepository = carRepository;
    }

    /**
     * Creates a new bicycle from a DTO.
     * 
     * @param request CreateBicycleRequest DTO
     * @return VehicleResponse DTO with persisted bicycle data
     * @throws IllegalArgumentException if request data is invalid
     */
    public VehicleResponse createBicycle(CreateBicycleRequest request) {
        validateProviderId(request.providerId());
        Location location = dtoToLocation(request.location());

        Bicycle bicycle = VehicleFactory.createBicycle(location, request.providerId(), request.costPerMinute());
        VehicleEntity savedEntity = vehicleRepository.save(VehicleMapper.toEntity(bicycle));
        Bicycle savedBicycle = (Bicycle) VehicleMapper.toDomain(savedEntity);

        return vehicleToResponse(savedBicycle);
    }

    /**
     * Creates a new scooter from a DTO.
     * 
     * @param request CreateScooterRequest DTO
     * @return VehicleResponse DTO with persisted scooter data
     * @throws IllegalArgumentException if request data is invalid
     */
    public VehicleResponse createScooter(CreateScooterRequest request) {
        validateProviderId(request.providerId());
        Location location = dtoToLocation(request.location());

        Scooter scooter = VehicleFactory.createScooter(location, request.providerId(), request.costPerMinute(), request.maxRange());
        VehicleEntity savedEntity = vehicleRepository.save(VehicleMapper.toEntity(scooter));
        Scooter savedScooter = (Scooter) VehicleMapper.toDomain(savedEntity);

        return vehicleToResponse(savedScooter);
    }

    /**
     * Creates a new car from a DTO.
     * 
     * @param request CreateCarRequest DTO
     * @return VehicleResponse DTO with persisted car data
     * @throws IllegalArgumentException if request data is invalid
     */
    public VehicleResponse createCar(CreateCarRequest request) {
        validateProviderId(request.providerId());
        Location location = dtoToLocation(request.location());

        Car car = VehicleFactory.createCar(location, request.providerId(), request.costPerMinute(), 
                request.licensePlate(), request.seatingCapacity());
        VehicleEntity savedEntity = vehicleRepository.save(VehicleMapper.toEntity(car));
        Car savedCar = (Car) VehicleMapper.toDomain(savedEntity);

        return vehicleToResponse(savedCar);
    }

    /**
     * Retrieves a vehicle by ID.
     * 
     * @param vehicleId ID of the vehicle to retrieve
     * @return VehicleResponse DTO if found
     * @throws IllegalArgumentException if vehicle not found
     */
    public VehicleResponse getVehicleById(Long vehicleId) {
        VehicleEntity entity = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + vehicleId));
        Vehicle vehicle = VehicleMapper.toDomain(entity);
        return vehicleToResponse(vehicle);
    }

    /**
     * Retrieves all vehicles.
     * 
     * @return List of VehicleResponse DTOs
     */
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(entity -> vehicleToResponse(VehicleMapper.toDomain(entity)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all vehicles with a specific status.
     * 
     * @param status Status to filter by
     * @return List of VehicleResponse DTOs with the specified status
     */
    public List<VehicleResponse> getVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status).stream()
                .map(entity -> vehicleToResponse(VehicleMapper.toDomain(entity)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all vehicles owned by a specific provider.
     * 
     * @param providerId ID of the provider
     * @return List of VehicleResponse DTOs owned by the provider
     */
    public List<VehicleResponse> getVehiclesByProviderId(Long providerId) {
        return vehicleRepository.findByProviderId(providerId).stream()
                .map(entity -> vehicleToResponse(VehicleMapper.toDomain(entity)))
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a vehicle.
     * 
     * @param vehicleId ID of the vehicle to update
     * @param newStatus New status to set
     * @return Updated VehicleResponse DTO
     * @throws IllegalArgumentException if vehicle not found
     */
    public VehicleResponse updateVehicleStatus(Long vehicleId, VehicleStatus newStatus) {
        VehicleEntity entity = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + vehicleId));
        
        entity.setStatus(newStatus);
        VehicleEntity updated = vehicleRepository.save(entity);
        Vehicle vehicle = VehicleMapper.toDomain(updated);
        
        return vehicleToResponse(vehicle);
    }

    /**
     * Updates the location of a vehicle.
     * 
     * @param vehicleId ID of the vehicle to update
     * @param locationDto New location DTO to set
     * @return Updated VehicleResponse DTO
     * @throws IllegalArgumentException if vehicle not found or location is invalid
     */
    public VehicleResponse updateVehicleLocation(Long vehicleId, LocationDto locationDto) {
        VehicleEntity entity = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + vehicleId));
        
        Location location = dtoToLocation(locationDto);
        LocationEmbeddable embeddable = new LocationEmbeddable(location.latitude(), location.longitude());
        entity.setLocation(embeddable);
        VehicleEntity updated = vehicleRepository.save(entity);
        Vehicle vehicle = VehicleMapper.toDomain(updated);
        
        return vehicleToResponse(vehicle);
    }

    /**
     * Deletes a vehicle by ID.
     * 
     * @param vehicleId ID of the vehicle to delete
     * @throws IllegalArgumentException if vehicle not found
     */
    public void deleteVehicle(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new IllegalArgumentException("Vehicle not found with id: " + vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }

    // Helper methods

    // Converts a domain Vehicle object to a VehicleResponse DTO.
    // Note: Handles all vehicle subtypes (Bicycle, Scooter, Car).
    private VehicleResponse vehicleToResponse(Vehicle vehicle) {
        LocationDto locationDto = locationToDto(vehicle.getLocation());
        
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


        return new VehicleResponse(
                vehicle.getId(),
                type,
                vehicle.getStatus().name(),
                locationDto,
                vehicle.getProviderId(),
                vehicle.getCostPerMinute(),
                maxRange,
                licensePlate,
                seatingCapacity
        );
    }

    // Converts a LocationDto to a domain Location record.
    private Location dtoToLocation(LocationDto dto) {
        return new Location(dto.latitude(), dto.longitude());
    }

     // Converts a domain Location record to a LocationDto.
    private LocationDto locationToDto(Location location) {
        return new LocationDto(location.latitude(), location.longitude());
    }

    // Validates that providerId is not null.
    // NOTE: If more validation is needed, it can be added here
    private void validateProviderId(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID cannot be null");
        }
    }
}
