package com.thehorselegend.summs.domain.payment.calculation;

/**
 * Applies a fixed discount amount. The total will never go below zero.
 */
public class DiscountDecorator extends PaymentDecorator {

    private final double discountAmount;

    public DiscountDecorator(Payment wrappedPayment, double discountAmount) {
        super(wrappedPayment);
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        this.discountAmount = discountAmount;
    }

    @Override
    public double getAmount() {
        return Math.max(0, wrappedPayment.getAmount() - discountAmount);
    }

    @Override
    public String getDescription() {
        return wrappedPayment.getDescription() + " - discount";
    }
}
