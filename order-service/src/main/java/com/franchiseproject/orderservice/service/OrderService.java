package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.CheckoutRequest;
import com.franchiseproject.orderservice.dto.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.response.OrderByCustomerResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Order> getAll();
    Order createOrder(CreateOrderRequest request);
    UUID checkoutOnline(CheckoutRequest request);
    void cancelOrder(UUID orderId, UUID customerId);
    void updateOrderStatus(UUID orderId, OrderStatus newStatus);
    List<OrderByCustomerResponse> getOrderByCustomerId(UUID customerId);
}
