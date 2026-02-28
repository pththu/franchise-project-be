package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.CheckoutRequest;
import com.franchiseproject.orderservice.dto.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
//import com.franchiseproject.orderservice.dto.response.OrderResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);
    UUID checkoutOnline(CheckoutRequest request);
    void cancelOrder(UUID orderId, UUID customerId);
    void updateOrderStatus(UUID orderId, OrderStatus newStatus);
    List<OrderResponse> getOrderByCustomerId(UUID customerId);
    void assignStaff(UUID orderId, UUID staffId);
    void markSpecial(UUID orderId);
    void estimateDeliveryTime(UUID orderId, Instant estimatedTime);
    List<OrderResponse> getAll();
    void addAddressOnlineOrder(AddAddressRequest request);
    String getAddressOnlineOrder(UUID customerId);
    Order updateOrder(UUID orderId, UpdateOrderRequest request);
}
