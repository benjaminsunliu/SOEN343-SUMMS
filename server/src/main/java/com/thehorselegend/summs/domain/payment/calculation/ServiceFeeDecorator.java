package com.thehorselegend.summs.domain.payment.calculation;

/**
 * Adds a fixed service fee to the payment.
 */
public class ServiceFeeDecorator extends PaymentDecorator {

    private final double serviceFeeAmount;

    public ServiceFeeDecorator(Payment wrappedPayment, double serviceFeeAmount) {
        super(wrappedPayment);
        if (serviceFeeAmount < 0) {
            throw new IllegalArgumentException("Service fee cannot be negative");
        }
        this.serviceFeeAmount = serviceFeeAmount;
    }

    @Override
    public double getAmount() {
        return wrappedPayment.getAmount() + serviceFeeAmount;
    }

    @Override
    public String getDescription() {
        return wrappedPayment.getDescription() + " + service fee";
    }
}
