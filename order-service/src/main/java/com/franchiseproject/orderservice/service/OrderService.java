package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
//import com.franchiseproject.orderservice.dto.response.OrderResponse;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    UUID createOrder(CreateOrderRequest request);

    void cancelOrder(UUID orderId, UUID customerId);

    void updateOrderStatus(UUID orderId, OrderStatus newStatus);

    List<OrderResponse> getOrderByCustomerId(UUID customerId);

    void assignStaff(UUID orderId, UUID staffId);

    void markSpecial(UUID orderId);

    void estimateDeliveryTime(UUID orderId, Instant estimatedTime);

    List<OrderResponse> getAll();

    void addAddressOnlineOrder(AddAddressRequest request);

    String getAddressOnlineOrder(UUID customerId);

    Page<OrderResponse> getOrdersByFranchiseAndStatus(
            UUID franchiseId,
            OrderStatus status,
            int page,
            int size
    );

    Page<OrderResponse> getOrdersByFranchiseAndFilters(
            UUID franchiseId,
            OrderStatus status,
            TypeOrder typeOrder,
            int page,
            int size
    );

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, int page, int size);
    Page<OrderResponse> getOrdersByFilters(OrderStatus status, TypeOrder typeOrder, int page, int size);

    List<OrderResponse> searchOrderById(String keyword);

    List<OrderResponse> searchOrders(UUID franchiseId, String keyword);

    Page<OrderResponse> getOrdersByCustomerIdAndStatus(UUID customerId, OrderStatus status, int page, int size);

    OrderResponse getOrderById(UUID orderId);

    void updatePaymentResult(com.franchiseproject.orderservice.dto.request.PaymentResultRequest request);
}
