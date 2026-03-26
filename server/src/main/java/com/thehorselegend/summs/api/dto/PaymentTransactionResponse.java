package com.thehorselegend.summs.api.dto;

import java.time.LocalDateTime;

public record PaymentTransactionResponse(
        Long id,
        Long reservationId,
        Long userId,
        Long providerId,
        String paymentMethod,
        double amount,
        boolean success,
        String message,
        String processorTransactionId,
        String paymentAuthorizationCode,
        LocalDateTime createdAt
) {
}
