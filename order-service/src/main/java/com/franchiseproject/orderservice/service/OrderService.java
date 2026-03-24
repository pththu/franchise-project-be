package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.request.PaymentResultRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
//import com.franchiseproject.orderservice.dto.response.OrderResponse;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import org.springframework.data.domain.Page;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    PaymentQRResponse createOrder(CreateOrderRequest request);

    PaymentQRResponse handleReserve(Order order, CreateOrderRequest request, BigDecimal totalItems);

    PaymentQRResponse handlePayment(Order order, CreateOrderRequest request);

    void handlePaymentResult(PaymentResultRequest paymentResultRequest);

    void cancelOrder(UUID orderId, UUID customerId);

    void updateOrderStatus(UUID orderId, OrderStatus newStatus);

    List<OrderResponse> getOrderByCustomerId(UUID customerId);

    void assignStaff(UUID orderId, UUID staffId);

    void markSpecial(UUID orderId);

    void estimateDeliveryTime(UUID orderId, Instant estimatedTime);

    List<OrderResponse> getAll();

    void addAddressOnlineOrder(AddAddressRequest request);

    String getAddressOnlineOrder(UUID customerId);


    Page<OrderResponse> getOrdersByFranchiseAndFilters(
            UUID franchiseId,
            OrderStatus status,
            TypeOrder typeOrder,
            int page,
            int size
    );

    Page<OrderResponse> getOrdersByFilters(OrderStatus status, TypeOrder typeOrder, int page, int size);

    List<OrderResponse> searchOrderById(String keyword);

    List<OrderResponse> searchOrders(UUID franchiseId, String keyword);

    Page<OrderResponse> getOrdersByCustomerIdAndStatus(UUID customerId, OrderStatus status, int page, int size);

    OrderResponse getOrderById(UUID orderId);
}
