package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.repository.OrderRepository;
import com.franchiseproject.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public void assignStaff(UUID orderId, UUID staffId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setAssignedStaffId(staffId);

        orderRepository.save(order);
    }
    @Override
    public void markSpecial(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setIsSpecial(true);

        orderRepository.save(order);
    }
    @Override
    public void estimateDeliveryTime(UUID orderId, Instant estimatedTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setEstimatedDeliveryTime(estimatedTime);

        orderRepository.save(order);
    }
}