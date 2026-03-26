package com.thehorselegend.summs.api.dto;

public record ProcessReservationPaymentResponse(
        boolean success,
        String message,
        String transactionId,
        String paymentAuthorizationCode,
        String paymentMethod,
        double amount,
        String details
) {
}
