package com.thehorselegend.summs.infrastructure.payment;

import com.thehorselegend.summs.domain.payment.method.PaymentMethodDetails;
import com.thehorselegend.summs.domain.payment.method.PaymentMethodStrategy;
import com.thehorselegend.summs.domain.payment.method.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulated in-app wallet payment processing.
 */
@Component
public class WalletPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public String getMethodName() {
        return "WALLET";
    }

    @Override
    public PaymentResult processPayment(double amount, PaymentMethodDetails details) {
        String txId = "WL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.failure("Wallet payment is not enabled in this simulation", txId);
    }
}
