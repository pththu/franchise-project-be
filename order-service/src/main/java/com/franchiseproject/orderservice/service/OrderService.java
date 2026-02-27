package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Order> getAll();
    void assignStaff(UUID orderId, UUID staffId);
    void markSpecial(UUID orderId);
    void estimateDeliveryTime(UUID orderId, Instant estimatedTime);
}
