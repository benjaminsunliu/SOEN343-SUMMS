package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.AddressSuggestionDto;
import com.thehorselegend.summs.application.service.reservation.AddressGeocodingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final AddressGeocodingService geocodingService;

    public LocationController(AddressGeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/suggestions")
    public List<AddressSuggestionDto> suggestAddresses(
            @RequestParam("query") String query,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "limit", defaultValue = "6") int limit
    ) {
        return geocodingService.suggestAddresses(query, city, limit);
    }

    @GetMapping("/cities")
    public List<String> suggestCities(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "6") int limit
    ) {
        return geocodingService.suggestCities(query, limit);
    }

    @GetMapping("/reverse")
    public AddressSuggestionDto reverseGeocode(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude
    ) {
        return geocodingService.reverseGeocode(latitude, longitude);
    }
}
