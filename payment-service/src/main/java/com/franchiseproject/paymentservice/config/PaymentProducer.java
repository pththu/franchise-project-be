package com.franchiseproject.paymentservice.config;

import com.franchiseproject.paymentservice.dto.request.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class PaymentProducer {
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentResult(PaymentResultEvent event) {
        kafkaTemplate.send("payment-result", event);
    }
}
