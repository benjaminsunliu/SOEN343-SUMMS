package com.thehorselegend.summs.api.dto;

public record AuthResponse(
        Long id,
        String name,
        String email,
        String role,
        String message
) {
}