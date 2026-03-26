package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.payment.transaction.PaymentTransaction;

public final class PaymentTransactionMapper {

    private PaymentTransactionMapper() {
    }

    public static PaymentTransactionEntity toEntity(PaymentTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        return new PaymentTransactionEntity(
                transaction.id(),
                transaction.reservationId(),
                transaction.userId(),
                transaction.providerId(),
                transaction.paymentMethod(),
                transaction.amount(),
                transaction.success(),
                transaction.message(),
                transaction.processorTransactionId(),
                transaction.paymentAuthorizationCode(),
                transaction.createdAt()
        );
    }

    public static PaymentTransaction toDomain(PaymentTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PaymentTransaction(
                entity.getId(),
                entity.getReservationId(),
                entity.getUserId(),
                entity.getProviderId(),
                entity.getPaymentMethod(),
                entity.getAmount(),
                entity.isSuccess(),
                entity.getMessage(),
                entity.getProcessorTransactionId(),
                entity.getPaymentAuthorizationCode(),
                entity.getCreatedAt()
        );
    }
}
