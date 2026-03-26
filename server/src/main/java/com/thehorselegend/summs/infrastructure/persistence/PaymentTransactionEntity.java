package com.thehorselegend.summs.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_transactions",
        indexes = {
                @Index(name = "idx_payment_transactions_reservation_id", columnList = "reservation_id"),
                @Index(name = "idx_payment_transactions_user_id_created_at", columnList = "user_id,created_at"),
                @Index(name = "idx_payment_transactions_provider_id_created_at", columnList = "provider_id,created_at")
        }
)
public class PaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "processor_transaction_id", nullable = false, length = 64)
    private String processorTransactionId;

    @Column(name = "payment_authorization_code", length = 80)
    private String paymentAuthorizationCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PaymentTransactionEntity() {
    }

    public PaymentTransactionEntity(Long id,
                                    Long reservationId,
                                    Long userId,
                                    Long providerId,
                                    String paymentMethod,
                                    double amount,
                                    boolean success,
                                    String message,
                                    String processorTransactionId,
                                    String paymentAuthorizationCode,
                                    LocalDateTime createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.providerId = providerId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.success = success;
        this.message = message;
        this.processorTransactionId = processorTransactionId;
        this.paymentAuthorizationCode = paymentAuthorizationCode;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProcessorTransactionId() {
        return processorTransactionId;
    }

    public void setProcessorTransactionId(String processorTransactionId) {
        this.processorTransactionId = processorTransactionId;
    }

    public String getPaymentAuthorizationCode() {
        return paymentAuthorizationCode;
    }

    public void setPaymentAuthorizationCode(String paymentAuthorizationCode) {
        this.paymentAuthorizationCode = paymentAuthorizationCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
