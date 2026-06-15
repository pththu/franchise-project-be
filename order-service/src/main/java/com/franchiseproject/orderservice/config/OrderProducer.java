package com.franchiseproject.orderservice.config;

import com.franchiseproject.orderservice.dto.request.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.request.CreatePaymentEvent;
import com.franchiseproject.orderservice.dto.request.OrderCreatedEvent;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.entity.Order;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {
    private KafkaTemplate<String, Object> kafkaTemplate;
    public void sendCreatePaymentEvent(Order order, CreateOrderRequest request) {
        CreatePaymentEvent event = new CreatePaymentEvent();
        event.setOrderId(order.getId());
        event.setPaymentMethodId(request.getPaymentMethodId());
        kafkaTemplate.send("order-created", order.getId().toString(), event);
    }
}
