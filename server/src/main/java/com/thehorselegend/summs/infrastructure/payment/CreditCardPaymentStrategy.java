package com.thehorselegend.summs.infrastructure.payment;

import com.thehorselegend.summs.domain.payment.method.PaymentMethodDetails;
import com.thehorselegend.summs.domain.payment.method.PaymentMethodStrategy;
import com.thehorselegend.summs.domain.payment.method.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulated credit-card payment processing.
 */
@Component
public class CreditCardPaymentStrategy implements PaymentMethodStrategy {

    private static final String TEST_CARD_NUMBER = "12345678";

    @Override
    public String getMethodName() {
        return "CREDIT_CARD";
    }

    @Override
    public PaymentResult processPayment(double amount, PaymentMethodDetails details) {
        String txId = "CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean isValidCard = details != null && TEST_CARD_NUMBER.equals(details.creditCardNumber());

        if (isValidCard) {
            return PaymentResult.success("Credit card payment approved", txId);
        }
        return PaymentResult.failure("Credit card payment declined (use test card 12345678)", txId);
    }
}
