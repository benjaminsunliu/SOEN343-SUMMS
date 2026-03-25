package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.PaymentRequestDTO;
import com.thehorselegend.summs.api.dto.PaymentResponseDTO;
import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.application.service.payment.PaymentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private IPaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void processPaymentReturns200OnSuccess() {
        PaymentRequestDTO request = new PaymentRequestDTO("Alex Citizen", "4242424242424242", 21.5);
        when(paymentService.processPayment("Alex Citizen", "4242424242424242", 21.5))
                .thenReturn(PaymentResult.success("PAY-abc"));

        ResponseEntity<PaymentResponseDTO> response = paymentController.processPayment(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().status());
        assertEquals("PAY-abc", response.getBody().paymentToken());
    }

    @Test
    void processPaymentReturns402OnDecline() {
        PaymentRequestDTO request = new PaymentRequestDTO("Alex Citizen", "4000000000000002", 21.5);
        when(paymentService.processPayment("Alex Citizen", "4000000000000002", 21.5))
                .thenReturn(PaymentResult.declined());

        ResponseEntity<PaymentResponseDTO> response = paymentController.processPayment(request);

        assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DECLINED", response.getBody().status());
    }
}
