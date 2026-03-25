package com.thehorselegend.summs.infrastructure.payment;

import com.thehorselegend.summs.application.service.payment.IPaymentService;
import com.thehorselegend.summs.infrastructure.payment.decorator.DeclineTestCardPaymentDecorator;
import com.thehorselegend.summs.infrastructure.payment.decorator.LatencySimulationPaymentDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfiguration {

    @Bean
    public IPaymentService paymentService() {
        IPaymentService gateway = new MockStripeAdapter();
        IPaymentService withDeclineRule = new DeclineTestCardPaymentDecorator(gateway);
        return new LatencySimulationPaymentDecorator(withDeclineRule);
    }
}
