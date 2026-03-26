package com.thehorselegend.summs.application.service.payment;

import com.thehorselegend.summs.domain.payment.transaction.PaymentTransaction;
import com.thehorselegend.summs.infrastructure.persistence.PaymentTransactionEntity;
import com.thehorselegend.summs.infrastructure.persistence.PaymentTransactionMapper;
import com.thehorselegend.summs.infrastructure.persistence.PaymentTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentTransactionService(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    public PaymentTransaction recordTransaction(Long reservationId,
                                                Long userId,
                                                Long providerId,
                                                String paymentMethod,
                                                double amount,
                                                boolean success,
                                                String message,
                                                String processorTransactionId,
                                                String paymentAuthorizationCode) {
        PaymentTransaction transaction = new PaymentTransaction(
                null,
                reservationId,
                userId,
                providerId,
                paymentMethod,
                amount,
                success,
                message,
                processorTransactionId,
                paymentAuthorizationCode,
                LocalDateTime.now()
        );

        PaymentTransactionEntity savedEntity =
                paymentTransactionRepository.save(PaymentTransactionMapper.toEntity(transaction));
        return PaymentTransactionMapper.toDomain(savedEntity);
    }

    public List<PaymentTransaction> getTransactionsForUser(Long userId) {
        return paymentTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PaymentTransactionMapper::toDomain)
                .toList();
    }

    public List<PaymentTransaction> getTransactionsForProvider(Long providerId) {
        return paymentTransactionRepository.findByProviderIdOrderByCreatedAtDesc(providerId).stream()
                .map(PaymentTransactionMapper::toDomain)
                .toList();
    }

    public ProviderPaymentAnalytics getProviderAnalytics(Long providerId) {
        List<PaymentTransaction> transactions = getTransactionsForProvider(providerId);
        int total = transactions.size();

        int successful = 0;
        double totalRevenue = 0;
        Map<String, Double> revenueByMethod = new HashMap<>();

        for (PaymentTransaction transaction : transactions) {
            if (!transaction.success()) {
                continue;
            }

            successful += 1;
            totalRevenue += transaction.amount();
            revenueByMethod.merge(transaction.paymentMethod(), transaction.amount(), Double::sum);
        }

        int failed = total - successful;
        double successRate = total == 0 ? 0 : ((double) successful / total) * 100.0;

        return new ProviderPaymentAnalytics(
                total,
                successful,
                failed,
                totalRevenue,
                successRate,
                revenueByMethod
        );
    }

    public record ProviderPaymentAnalytics(
            int totalTransactions,
            int successfulTransactions,
            int failedTransactions,
            double totalRevenue,
            double successRatePercentage,
            Map<String, Double> revenueByPaymentMethod
    ) {
    }
}
