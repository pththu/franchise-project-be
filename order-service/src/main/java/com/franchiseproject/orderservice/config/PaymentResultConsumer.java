package com.franchiseproject.orderservice.config;

import com.franchiseproject.orderservice.dto.response.PaymentResultEvent;
import org.springframework.kafka.annotation.KafkaListener;

public class PaymentResultConsumer {
    @KafkaListener(topics = "payment-result")
    public void consume(PaymentResultEvent event) {
        System.out.println("Payment result: " + event.getStatus());

        // update order status
    }
}
