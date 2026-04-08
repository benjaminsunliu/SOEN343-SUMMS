package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.ProcessReservationPaymentRequest;
import com.thehorselegend.summs.api.dto.ProcessReservationPaymentResponse;
import com.thehorselegend.summs.application.service.payment.PaymentApplicationService;
import com.thehorselegend.summs.api.dto.PaymentTransactionResponse;
import com.thehorselegend.summs.api.dto.ProviderPaymentAnalyticsResponse;
import com.thehorselegend.summs.application.service.payment.PaymentTransactionService;
import com.thehorselegend.summs.domain.payment.calculation.Payment;
import com.thehorselegend.summs.domain.payment.method.PaymentMethodDetails;
import com.thehorselegend.summs.domain.payment.method.PaymentResult;
import com.thehorselegend.summs.domain.payment.transaction.PaymentTransaction;
import com.thehorselegend.summs.domain.reservation.Reservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.domain.user.UserRole;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import com.thehorselegend.summs.infrastructure.persistence.VehicleRepository;
import com.thehorselegend.summs.application.service.reservation.VehicleReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;
    private final PaymentTransactionService paymentTransactionService;
    private final VehicleReservationService reservationService;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public PaymentController(PaymentApplicationService paymentApplicationService,
                             PaymentTransactionService paymentTransactionService,
                             VehicleReservationService reservationService,
                             VehicleRepository vehicleRepository,
                             UserRepository userRepository) {
        this.paymentApplicationService = paymentApplicationService;
        this.paymentTransactionService = paymentTransactionService;
        this.reservationService = reservationService;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ProcessReservationPaymentResponse processReservationPayment(
            @PathVariable Long reservationId,
            @Valid @RequestBody ProcessReservationPaymentRequest request,
            Authentication authentication
    ) {
        UserEntity user = resolveAuthenticatedUser(authentication);
        Long userId = user.getId();
        Reservation reservation = reservationService.getUserReservationById(reservationId, userId);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reservation must be confirmed before payment"
            );
        }

        PaymentApplicationService.PaymentOptions options = new PaymentApplicationService.PaymentOptions(
                true,
                request.serviceFeeAmount(),
                true,
                request.taxRate(),
                request.includeInsuranceFee(),
                request.insuranceFeeAmount(),
                request.includeDiscount(),
                request.discountAmount()
        );

        Payment payment = paymentApplicationService.buildPayment(reservation, options);
        PaymentMethodDetails paymentMethodDetails = new PaymentMethodDetails(
                request.creditCardNumber(),
                request.paypalEmail(),
                request.paypalPassword()
        );

        PaymentResult paymentResult = paymentApplicationService.processReservationPayment(
                reservation,
                options,
                request.paymentMethod(),
                paymentMethodDetails
        );

        String paymentMethod = request.paymentMethod().toUpperCase(Locale.ROOT);
        String paymentAuthorizationCode = paymentResult.isSuccess()
                ? "PAY-" + paymentResult.getTransactionId()
                : null;
        Long providerId = vehicleRepository.findById(reservation.getReservableId())
                .map(vehicle -> vehicle.getProviderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found for reservation"
                ));

        paymentTransactionService.recordTransaction(
                reservation.getId(),
                reservation.getUserId(),
                providerId,
                paymentMethod,
                payment.getAmount(),
                paymentResult.isSuccess(),
                paymentResult.getMessage(),
                paymentResult.getTransactionId(),
                paymentAuthorizationCode
        );

        return new ProcessReservationPaymentResponse(
                paymentResult.isSuccess(),
                paymentResult.getMessage(),
                paymentResult.getTransactionId(),
                paymentAuthorizationCode,
                paymentMethod,
                payment.getAmount(),
                payment.getDescription()
        );
    }

    @GetMapping("/transactions/me")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public List<PaymentTransactionResponse> getCurrentUserTransactions(Authentication authentication) {
        UserEntity user = resolveAuthenticatedUser(authentication);
        return paymentTransactionService.getTransactionsForUser(user.getId()).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @GetMapping("/transactions/provider/me")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public List<PaymentTransactionResponse> getCurrentProviderTransactions(Authentication authentication) {
        UserEntity user = resolveAuthenticatedUser(authentication);
        ensureProviderOrAdmin(user);

        return paymentTransactionService.getTransactionsForProvider(user.getId()).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @GetMapping("/transactions/analytics/provider/me")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ProviderPaymentAnalyticsResponse getCurrentProviderAnalytics(Authentication authentication) {
        UserEntity user = resolveAuthenticatedUser(authentication);
        ensureProviderOrAdmin(user);

        PaymentTransactionService.ProviderPaymentAnalytics analytics =
                paymentTransactionService.getProviderAnalytics(user.getId());

        return new ProviderPaymentAnalyticsResponse(
                analytics.totalTransactions(),
                analytics.successfulTransactions(),
                analytics.failedTransactions(),
                analytics.totalRevenue(),
                analytics.successRatePercentage(),
                analytics.revenueByPaymentMethod()
        );
    }

    private PaymentTransactionResponse toTransactionResponse(PaymentTransaction transaction) {
        return new PaymentTransactionResponse(
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

    private void ensureProviderOrAdmin(UserEntity user) {
        if (user.getRole() != UserRole.PROVIDER && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Provider role required");
        }
    }

    private UserEntity resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Authenticated user no longer exists"));
    }
}
