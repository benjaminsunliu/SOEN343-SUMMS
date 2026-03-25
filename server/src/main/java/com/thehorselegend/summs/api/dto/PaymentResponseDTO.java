package com.thehorselegend.summs.api.dto;

public record PaymentResponseDTO(
        String status,
        String message,
        String paymentToken
) {
}
