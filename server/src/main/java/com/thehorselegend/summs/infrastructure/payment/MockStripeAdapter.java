package com.thehorselegend.summs.infrastructure.payment;

import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.application.service.payment.PaymentResult;

import java.util.UUID;

public class MockStripeAdapter implements IPaymentService {

    @Override
    public PaymentResult processPayment(String cardHolderName, String cardNumber, double amount) {
        if (amount <= 0) {
            return PaymentResult.declined();
        }

        // Base gateway always returns a tokenized success for valid amounts.
        String token = "PAY-" + UUID.randomUUID();
        return PaymentResult.success(token);
    }
}
