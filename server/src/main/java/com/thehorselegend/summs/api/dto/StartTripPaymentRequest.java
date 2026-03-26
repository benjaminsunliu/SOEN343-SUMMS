package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.NotBlank;

public record StartTripPaymentRequest(
        @NotBlank(message = "Payment authorization code is required")
        String paymentAuthorizationCode
) {
}
