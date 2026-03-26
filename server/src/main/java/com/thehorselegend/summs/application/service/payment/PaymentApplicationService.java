package com.thehorselegend.summs.application.service.payment;

import com.thehorselegend.summs.domain.payment.calculation.DiscountDecorator;
import com.thehorselegend.summs.domain.payment.calculation.InsuranceFeeDecorator;
import com.thehorselegend.summs.domain.payment.calculation.Payment;
import com.thehorselegend.summs.domain.payment.method.PaymentMethodDetails;
import com.thehorselegend.summs.domain.payment.method.PaymentMethodStrategy;
import com.thehorselegend.summs.domain.payment.method.PaymentResult;
import com.thehorselegend.summs.domain.payment.calculation.ReservationPayment;
import com.thehorselegend.summs.domain.payment.calculation.ServiceFeeDecorator;
import com.thehorselegend.summs.domain.payment.calculation.TaxDecorator;
import com.thehorselegend.summs.domain.reservation.Reservation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates the payment use case:
 * 1) build payment amount via decorators
 * 2) execute selected payment method strategy
 */
@Service
public class PaymentApplicationService {

    private final Map<String, PaymentMethodStrategy> strategyByMethod;

    public PaymentApplicationService(List<PaymentMethodStrategy> strategies) {
        this.strategyByMethod = strategies.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getMethodName().toUpperCase(Locale.ROOT),
                        Function.identity()
                ));
    }

    public PaymentResult processReservationPayment(Reservation reservation,
                                                   PaymentOptions options,
                                                   String paymentMethod,
                                                   PaymentMethodDetails paymentMethodDetails) {
        Payment payment = new ReservationPayment(reservation);
        payment = applyDecorators(payment, options);

        PaymentMethodStrategy strategy = resolveStrategy(paymentMethod);
        PaymentResult rawResult = strategy.processPayment(payment.getAmount(), paymentMethodDetails);

        String message = rawResult.getMessage()
                + " | amount=" + payment.getAmount()
                + " | details=" + payment.getDescription();

        return new PaymentResult(rawResult.isSuccess(), message, rawResult.getTransactionId());
    }

    public Payment buildPayment(Reservation reservation, PaymentOptions options) {
        Payment payment = new ReservationPayment(reservation);
        return applyDecorators(payment, options);
    }

    private Payment applyDecorators(Payment payment, PaymentOptions options) {
        if (options == null) {
            return payment;
        }

        Payment decorated = payment;
        if (options.includeServiceFee()) {
            decorated = new ServiceFeeDecorator(decorated, options.serviceFeeAmount());
        }
        if (options.includeTax()) {
            decorated = new TaxDecorator(decorated, options.taxRate());
        }
        if (options.includeInsuranceFee()) {
            decorated = new InsuranceFeeDecorator(decorated, options.insuranceFeeAmount());
        }
        if (options.includeDiscount()) {
            decorated = new DiscountDecorator(decorated, options.discountAmount());
        }

        return decorated;
    }

    private PaymentMethodStrategy resolveStrategy(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        PaymentMethodStrategy strategy = strategyByMethod.get(paymentMethod.toUpperCase(Locale.ROOT));
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }

        return strategy;
    }

    /**
     * Simple options object to keep method calls readable.
     */
    public record PaymentOptions(
            boolean includeServiceFee,
            double serviceFeeAmount,
            boolean includeTax,
            double taxRate,
            boolean includeInsuranceFee,
            double insuranceFeeAmount,
            boolean includeDiscount,
            double discountAmount
    ) {
    }
}
