package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
import com.franchiseproject.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    void cancelOrder(UUID orderId, UUID customerId);

    void updateOrderStatus(UUID orderId, OrderStatus newStatus);

    List<OrderResponse> getOrderByCustomerId(UUID customerId);

    void assignStaff(UUID orderId, UUID staffId);

    void markSpecial(UUID orderId);

    void estimateDeliveryTime(UUID orderId, Instant estimatedTime);

    List<OrderResponse> getAll();

    void addAddressOnlineOrder(AddAddressRequest request);

    String getAddressOnlineOrder(UUID customerId);

    OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request);

    Page<OrderResponse> getOrdersByFranchiseAndStatus(
            UUID franchiseId,
            OrderStatus status,
            int page,
            int size
    );

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, int page, int size);

    List<OrderResponse> searchOrderById(String keyword);

    List<OrderResponse> searchOrders(UUID franchiseId, String keyword);

    Page<OrderResponse> getOrdersByCustomerIdAndStatus(UUID customerId, OrderStatus status, int page, int size);

    OrderResponse getOrderById(UUID orderId);
}
