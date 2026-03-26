package com.thehorselegend.summs.domain.payment.method;

/**
 * Result returned by simulated payment processing.
 */
public class PaymentResult {

    private final boolean success;
    private final String message;
    private final String transactionId;

    public PaymentResult(boolean success, String message, String transactionId) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
    }

    public static PaymentResult success(String message, String transactionId) {
        return new PaymentResult(true, message, transactionId);
    }

    public static PaymentResult failure(String message, String transactionId) {
        return new PaymentResult(false, message, transactionId);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
