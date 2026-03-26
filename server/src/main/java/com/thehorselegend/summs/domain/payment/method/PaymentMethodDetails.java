package com.thehorselegend.summs.domain.payment.method;

/**
 * Method-specific fields used by payment strategies.
 */
public record PaymentMethodDetails(
        String creditCardNumber,
        String paypalEmail,
        String paypalPassword
) {
}
