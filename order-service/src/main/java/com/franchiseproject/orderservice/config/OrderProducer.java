package com.franchiseproject.orderservice.config;

import com.franchiseproject.orderservice.dto.request.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class OrderProducer {
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send("order-created", event);
    }
}
