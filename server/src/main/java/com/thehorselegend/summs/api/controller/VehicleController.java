package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.*;
import com.thehorselegend.summs.application.service.VehicleService;
import com.thehorselegend.summs.domain.vehicle.VehicleStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for vehicle management.
 * Handles HTTP requests for creating, retrieving, updating, and deleting vehicles.
 * 
 * General structure:
 * - Receives DTOs in requests
 * - Delegates to service
 * - Returns DTOs in responses
 */
@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // NOTE: REST API Names are up for discussion. 
    // We can change them if we want. 
    // I just went with the most straightforward ones for now.
    
    // Create a new bicycle
    // POST /api/vehicles/bicycles
    @PostMapping("/bicycles")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createBicycle(@Valid @RequestBody CreateBicycleRequest request) {
        return vehicleService.createBicycle(request);
    }

    
    // Create a new scooter
    // POST /api/vehicles/scooters
    @PostMapping("/scooters")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createScooter(@Valid @RequestBody CreateScooterRequest request) {
        return vehicleService.createScooter(request);
    }

    // Create a new car
    // POST /api/vehicles/cars
    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createCar(@Valid @RequestBody CreateCarRequest request) {
        return vehicleService.createCar(request);
    }

    // Retrieve a vehicle by ID
    // GET /api/vehicles/{vehicleId}
    @GetMapping("/{vehicleId}")
    public VehicleResponse getVehicleById(@PathVariable Long vehicleId) {
        return vehicleService.getVehicleById(vehicleId);
    }

    // Retrieve all vehicles
    // GET /api/vehicles
    @GetMapping
    public List<VehicleResponse> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    // Retrieve all vehicles with a specific status
    // GET /api/vehicles/status/{status}
    @GetMapping("/status/{status}")
    public List<VehicleResponse> getVehiclesByStatus(@PathVariable VehicleStatus status) {
        return vehicleService.getVehiclesByStatus(status);
    }

    // Retrieve all vehicles owned by a specific provider
    // GET /api/vehicles/provider/{providerId}
    @GetMapping("/provider/{providerId}")
    public List<VehicleResponse> getVehiclesByProvider(@PathVariable Long providerId) {
        return vehicleService.getVehiclesByProviderId(providerId);
    }

    // Update a vehicle's status
    // PATCH /api/vehicles/{vehicleId}/status
    @PatchMapping("/{vehicleId}/status")
    public VehicleResponse updateVehicleStatus(
            @PathVariable Long vehicleId,
            @RequestParam VehicleStatus status) {
        return vehicleService.updateVehicleStatus(vehicleId, status);
    }

    // Update a vehicle's location
    // PATCH /api/vehicles/{vehicleId}/location
    @PatchMapping("/{vehicleId}/location")
    public VehicleResponse updateVehicleLocation(
            @PathVariable Long vehicleId,
            @Valid @RequestBody LocationDto location) {
        return vehicleService.updateVehicleLocation(vehicleId, location);
    }

    // Delete a vehicle
    // DELETE /api/vehicles/{vehicleId}
    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long vehicleId) {
        vehicleService.deleteVehicle(vehicleId);
    }
}
