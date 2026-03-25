package com.thehorselegend.summs.api.dto;

public record AddressSuggestionDto(
        String address,
        String city,
        Double latitude,
        Double longitude
) {
}
