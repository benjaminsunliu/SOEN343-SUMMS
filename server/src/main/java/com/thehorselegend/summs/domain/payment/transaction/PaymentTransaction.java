package com.thehorselegend.summs.domain.payment.transaction;

import java.time.LocalDateTime;

/**
 * Domain representation of a payment transaction linked to a reservation.
 */
public record PaymentTransaction(
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
