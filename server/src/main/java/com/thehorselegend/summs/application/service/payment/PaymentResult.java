package com.thehorselegend.summs.application.service.payment;

public record PaymentResult(
        boolean successful,
        String status,
        String message,
        String paymentToken
) {

    public static PaymentResult success(String paymentToken) {
        return new PaymentResult(true, "SUCCESS", "Payment approved.", paymentToken);
    }

    public static PaymentResult declined() {
        return new PaymentResult(false, "DECLINED", "Payment was declined.", null);
    }
}
