package com.thehorselegend.summs.infrastructure.config;

import com.thehorselegend.summs.application.service.ParkingFacilityService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ParkingDataBootstrap implements ApplicationRunner {

    private final ParkingFacilityService parkingFacilityService;

    public ParkingDataBootstrap(ParkingFacilityService parkingFacilityService) {
        this.parkingFacilityService = parkingFacilityService;
    }

    @Override
    public void run(ApplicationArguments args) {
        parkingFacilityService.validateBundledCatalogPresence();
    }
}
