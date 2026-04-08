package com.thehorselegend.summs.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for calculating CO₂ emissions saved by using sustainable mobility options.
 * Uses the formula: CO₂ saved (kg) = distance (km) × 0.12 kg CO₂/km
 */
@Service
public class Co2EmissionsService {

    private static final Logger logger = LoggerFactory.getLogger(Co2EmissionsService.class);

    // Emission factor: 0.12 kg CO₂ per km saved when using bikes/scooters instead of cars
    private static final double CO2_EMISSION_FACTOR_KG_PER_KM = 0.12;

    // Earth radius in kilometers (for Haversine formula)
    private static final int EARTH_RADIUS_KM = 6371;

    // Valid latitude range: -90 to 90
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;

    // Valid longitude range: -180 to 180
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    /**
     * Calculates CO₂ savings for a trip based on the distance traveled.
     *
     * @param startLatitude  the latitude of the trip start location
     * @param startLongitude the longitude of the trip start location
     * @param endLatitude    the latitude of the trip end location
     * @param endLongitude   the longitude of the trip end location
     * @return the CO₂ saved in kilograms, or 0.0 if the distance cannot be calculated
     */
    public Double calculateCo2Saved(double startLatitude, double startLongitude, 
                                     double endLatitude, double endLongitude) {
        try {
            // Validate coordinates
            if (!isValidCoordinate(startLatitude, startLongitude)) {
                logger.warn("Invalid start coordinates: lat={}, lon={}", startLatitude, startLongitude);
                return 0.0;
            }

            if (!isValidCoordinate(endLatitude, endLongitude)) {
                logger.warn("Invalid end coordinates: lat={}, lon={}", endLatitude, endLongitude);
                return 0.0;
            }

            double distanceKm = calculateHaversineDistance(startLatitude, startLongitude, 
                                                           endLatitude, endLongitude);
            
            // Check if distance is valid
            if (!Double.isFinite(distanceKm) || distanceKm < 0) {
                logger.warn("Invalid distance calculated: {}", distanceKm);
                return 0.0;
            }

            double co2Saved = distanceKm * CO2_EMISSION_FACTOR_KG_PER_KM;
            logger.debug("CO₂ calculation: distance={} km, co2Saved={} kg", distanceKm, co2Saved);
            
            return co2Saved;
        } catch (Exception e) {
            logger.error("Error calculating CO₂ for coordinates: ({},{})->({},{})", 
                    startLatitude, startLongitude, endLatitude, endLongitude, e);
            return 0.0;
        }
    }

    /**
     * Validates that a coordinate pair is within valid ranges.
     *
     * @param latitude  latitude in degrees (-90 to 90)
     * @param longitude longitude in degrees (-180 to 180)
     * @return true if valid, false otherwise
     */
    private boolean isValidCoordinate(double latitude, double longitude) {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude) &&
               !Double.isInfinite(latitude) && !Double.isInfinite(longitude) &&
               latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE &&
               longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    /**
     * Calculates the great-circle distance between two points on Earth using the Haversine formula.
     *
     * @param lat1 latitude of first point in degrees
     * @param lon1 longitude of first point in degrees
     * @param lat2 latitude of second point in degrees
     * @param lon2 longitude of second point in degrees
     * @return distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
}


