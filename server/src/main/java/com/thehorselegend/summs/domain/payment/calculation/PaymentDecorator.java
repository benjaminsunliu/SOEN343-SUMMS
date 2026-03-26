package com.thehorselegend.summs.domain.payment.calculation;

/**
 * Shared decorator parent. Concrete decorators adjust amount/description.
 */
public abstract class PaymentDecorator implements Payment {

    protected final Payment wrappedPayment;

    protected PaymentDecorator(Payment wrappedPayment) {
        if (wrappedPayment == null) {
            throw new IllegalArgumentException("Wrapped payment cannot be null");
        }
        this.wrappedPayment = wrappedPayment;
    }

    @Override
    public double getAmount() {
        return wrappedPayment.getAmount();
    }

    @Override
    public String getDescription() {
        return wrappedPayment.getDescription();
    }
}
