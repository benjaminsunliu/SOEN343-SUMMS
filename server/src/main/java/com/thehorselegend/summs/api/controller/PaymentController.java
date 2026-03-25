package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.PaymentRequestDTO;
import com.thehorselegend.summs.api.dto.PaymentResponseDTO;
import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.application.service.payment.PaymentResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        PaymentResult result = paymentService.processPayment(
                request.cardHolderName(),
                request.cardNumber(),
                request.amount()
        );

        PaymentResponseDTO body = new PaymentResponseDTO(
                result.status(),
                result.message(),
                result.paymentToken()
        );

        if (!result.successful()) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
        }

        return ResponseEntity.ok(body);
    }
}
