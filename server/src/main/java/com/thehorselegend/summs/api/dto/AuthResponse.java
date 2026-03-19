package com.thehorselegend.summs.api.dto;

public record AuthResponse(
                Long id,
                String name,
                String email,
                String role,
                String message,
                String token) {

        public AuthResponse(Long id, String name, String email, String role, String message) {
                this(id, name, email, role, message, null);
        }
}