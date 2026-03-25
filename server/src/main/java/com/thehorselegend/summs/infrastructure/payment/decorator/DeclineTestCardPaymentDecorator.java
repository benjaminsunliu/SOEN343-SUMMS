package com.thehorselegend.summs.infrastructure.payment.decorator;

import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.application.service.payment.PaymentResult;

import java.util.Set;

public class DeclineTestCardPaymentDecorator extends PaymentServiceDecorator {

    private static final Set<String> DECLINED_TEST_CARDS = Set.of(
            "4000000000000002",
            "4000000000009995"
    );

    public DeclineTestCardPaymentDecorator(IPaymentService delegate) {
        super(delegate);
    }

    @Override
    public PaymentResult processPayment(String cardHolderName, String cardNumber, double amount) {
        String normalizedCard = cardNumber == null ? "" : cardNumber.replaceAll("\\s+", "");
        if (DECLINED_TEST_CARDS.contains(normalizedCard)) {
            return PaymentResult.declined();
        }

        return delegate.processPayment(cardHolderName, cardNumber, amount);
    }
}
