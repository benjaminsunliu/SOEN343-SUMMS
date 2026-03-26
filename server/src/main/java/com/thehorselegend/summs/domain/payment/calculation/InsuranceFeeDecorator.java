package com.thehorselegend.summs.domain.payment.calculation;

/**
 * Adds a fixed insurance fee to the payment.
 */
public class InsuranceFeeDecorator extends PaymentDecorator {

    private final double insuranceFeeAmount;

    public InsuranceFeeDecorator(Payment wrappedPayment, double insuranceFeeAmount) {
        super(wrappedPayment);
        if (insuranceFeeAmount < 0) {
            throw new IllegalArgumentException("Insurance fee cannot be negative");
        }
        this.insuranceFeeAmount = insuranceFeeAmount;
    }

    @Override
    public double getAmount() {
        return wrappedPayment.getAmount() + insuranceFeeAmount;
    }

    @Override
    public String getDescription() {
        return wrappedPayment.getDescription() + " + insurance";
    }
}
