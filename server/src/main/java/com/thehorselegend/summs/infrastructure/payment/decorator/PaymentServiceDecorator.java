package com.thehorselegend.summs.infrastructure.payment.decorator;

import com.thehorselegend.summs.application.service.payment.IPaymentService;

public abstract class PaymentServiceDecorator implements IPaymentService {

    protected final IPaymentService delegate;

    protected PaymentServiceDecorator(IPaymentService delegate) {
        this.delegate = delegate;
    }
}
