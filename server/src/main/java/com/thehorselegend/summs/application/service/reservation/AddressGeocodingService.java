package com.thehorselegend.summs.application.service.reservation;

import com.thehorselegend.summs.api.dto.AddressSuggestionDto;
import com.thehorselegend.summs.domain.vehicle.Location;

import java.util.List;

public interface AddressGeocodingService {

    Location geocode(String address, String city);

    List<AddressSuggestionDto> suggestAddresses(String query, String city, int limit);

    List<String> suggestCities(String query, int limit);

    AddressSuggestionDto reverseGeocode(double latitude, double longitude);
}
