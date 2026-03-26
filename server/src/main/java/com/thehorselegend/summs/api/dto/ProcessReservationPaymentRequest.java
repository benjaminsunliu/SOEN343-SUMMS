package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProcessReservationPaymentRequest(
        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        String creditCardNumber,
        String paypalEmail,
        String paypalPassword,

        boolean includeServiceFee,
        @PositiveOrZero(message = "Service fee amount cannot be negative")
        double serviceFeeAmount,

        boolean includeTax,
        @PositiveOrZero(message = "Tax rate cannot be negative")
        double taxRate,

        boolean includeInsuranceFee,
        @PositiveOrZero(message = "Insurance fee amount cannot be negative")
        double insuranceFeeAmount,

        boolean includeDiscount,
        @PositiveOrZero(message = "Discount amount cannot be negative")
        double discountAmount
) {
}
