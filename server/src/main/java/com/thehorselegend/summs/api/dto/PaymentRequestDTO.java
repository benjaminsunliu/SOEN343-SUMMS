package com.thehorselegend.summs.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDTO(
        @NotBlank(message = "Card holder name is required")
        String cardHolderName,

        @NotBlank(message = "Card number is required")
        String cardNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        Double amount
) {
}
