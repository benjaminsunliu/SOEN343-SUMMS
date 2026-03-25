package com.thehorselegend.summs.infrastructure.payment.decorator;

import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.application.service.payment.PaymentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeclineTestCardPaymentDecoratorTest {

    @Mock
    private IPaymentService delegate;

    @Test
    void declinesKnownCardNumberWithoutCallingDelegate() {
        DeclineTestCardPaymentDecorator decorator = new DeclineTestCardPaymentDecorator(delegate);

        PaymentResult result = decorator.processPayment("Alex", "4000 0000 0000 0002", 25.0);

        assertFalse(result.successful());
        assertEquals("DECLINED", result.status());
        verifyNoInteractions(delegate);
    }

    @Test
    void forwardsUnknownCardNumberToDelegate() {
        DeclineTestCardPaymentDecorator decorator = new DeclineTestCardPaymentDecorator(delegate);
        when(delegate.processPayment("Alex", "4242424242424242", 25.0))
                .thenReturn(PaymentResult.success("PAY-1"));

        PaymentResult result = decorator.processPayment("Alex", "4242424242424242", 25.0);

        assertTrue(result.successful());
        assertEquals("PAY-1", result.paymentToken());
        verify(delegate).processPayment("Alex", "4242424242424242", 25.0);
    }
}
