package com.franchiseproject.paymentservice.config;

import com.franchiseproject.paymentservice.dto.request.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;

@RequiredArgsConstructor
public class OrderConsumer {
    private PaymentProducer producer;

    @KafkaListener(topics = "order-created")
    public void consume(OrderCreatedEvent event) {

        System.out.println("Processing payment for: " + event.getOrderId());

        PaymentResultEvent result = new PaymentResultEvent();
        result.setOrderId(event.getOrderId());

        // giả lập thanh toán
        result.setStatus("SUCCESS");

        producer.sendPaymentResult(result);
    }
}