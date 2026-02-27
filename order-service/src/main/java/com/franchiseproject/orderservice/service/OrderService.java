package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.response.OrderResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Order> getAll();
    void updateOrderStatus(UUID orderId, OrderStatus newStatus);
    List<OrderResponse> getOrderByCustomerId(UUID customerId);
    void addAddressOnlineOrder(AddAddressRequest request);
    String getAddressOnlineOrder(UUID customerId);
}
