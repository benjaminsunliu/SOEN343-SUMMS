package com.thehorselegend.summs.domain.payment.method;

/**
 * Strategy abstraction for payment methods.
 */
public interface PaymentMethodStrategy {

    String getMethodName();

    PaymentResult processPayment(double amount, PaymentMethodDetails details);
}
