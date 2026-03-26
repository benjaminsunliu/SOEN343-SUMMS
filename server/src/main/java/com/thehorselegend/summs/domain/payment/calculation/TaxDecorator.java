package com.thehorselegend.summs.domain.payment.calculation;

/**
 * Applies tax as a percentage (for example, 0.15 = 15%).
 */
public class TaxDecorator extends PaymentDecorator {

    private final double taxRate;

    public TaxDecorator(Payment wrappedPayment, double taxRate) {
        super(wrappedPayment);
        if (taxRate < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative");
        }
        this.taxRate = taxRate;
    }

    @Override
    public double getAmount() {
        return wrappedPayment.getAmount() * (1 + taxRate);
    }

    @Override
    public String getDescription() {
        return wrappedPayment.getDescription() + " + tax";
    }
}
