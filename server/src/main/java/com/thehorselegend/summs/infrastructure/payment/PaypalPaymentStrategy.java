package com.thehorselegend.summs.infrastructure.payment;

import com.thehorselegend.summs.domain.payment.method.PaymentMethodDetails;
import com.thehorselegend.summs.domain.payment.method.PaymentMethodStrategy;
import com.thehorselegend.summs.domain.payment.method.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulated PayPal payment processing.
 */
@Component
public class PaypalPaymentStrategy implements PaymentMethodStrategy {

    private static final String TEST_PAYPAL_EMAIL = "payment@test.com";
    private static final String TEST_PAYPAL_PASSWORD = "Test123";

    @Override
    public String getMethodName() {
        return "PAYPAL";
    }

    @Override
    public PaymentResult processPayment(double amount, PaymentMethodDetails details) {
        String txId = "PP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean isValidEmail = details != null
                && details.paypalEmail() != null
                && TEST_PAYPAL_EMAIL.equalsIgnoreCase(details.paypalEmail().trim());
        boolean isValidPassword = details != null && TEST_PAYPAL_PASSWORD.equals(details.paypalPassword());

        if (isValidEmail && isValidPassword) {
            return PaymentResult.success("PayPal payment completed", txId);
        }
        return PaymentResult.failure(
                "PayPal payment failed (use payment@test.com / Test123)",
                txId
        );
    }
}
