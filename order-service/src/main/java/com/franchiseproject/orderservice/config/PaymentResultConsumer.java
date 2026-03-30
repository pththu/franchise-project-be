package com.franchiseproject.orderservice.config;

import com.franchiseproject.orderservice.dto.response.PaymentResultEvent;
import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;

public class PaymentResultConsumer {
//    OrderRepository orderRepository;
//    @KafkaListener(topics = "payment-created")
//    public void handlePaymentResult(PaymentResultEvent result) {
//
//        Order order = orderRepository.findById(result.getOrderId())
//                .orElseThrow();
//
//        if ("SUCCESS".equals(result.getStatus())) {
//            order.setOrderStatus(OrderStatus.PAID);
//        } else {
//            order.setOrderStatus(OrderStatus.FAILED_PAYMENT);
//        }
//
//        orderRepository.save(order);
//    }
}
