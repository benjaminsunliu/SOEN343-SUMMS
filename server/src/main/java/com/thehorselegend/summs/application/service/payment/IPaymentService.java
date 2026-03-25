package com.thehorselegend.summs.application.service.payment;

public interface IPaymentService {

    PaymentResult processPayment(String cardHolderName, String cardNumber, double amount);
}
