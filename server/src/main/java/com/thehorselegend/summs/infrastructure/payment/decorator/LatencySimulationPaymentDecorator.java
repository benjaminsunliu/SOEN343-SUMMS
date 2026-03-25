package com.thehorselegend.summs.infrastructure.payment.decorator;

import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.application.service.payment.PaymentResult;

public class LatencySimulationPaymentDecorator extends PaymentServiceDecorator {

    public LatencySimulationPaymentDecorator(IPaymentService delegate) {
        super(delegate);
    }

    @Override
    public PaymentResult processPayment(String cardHolderName, String cardNumber, double amount) {
        simulateGatewayDelay();
        return delegate.processPayment(cardHolderName, cardNumber, amount);
    }

    private void simulateGatewayDelay() {
        try {
            Thread.sleep(900L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
