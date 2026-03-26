package com.thehorselegend.summs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    List<PaymentTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentTransactionEntity> findByProviderIdOrderByCreatedAtDesc(Long providerId);
}
