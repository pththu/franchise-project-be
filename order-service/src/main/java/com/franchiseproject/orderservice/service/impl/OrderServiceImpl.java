package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.dto.*;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import com.franchiseproject.orderservice.exception.BusinessException;
import com.franchiseproject.orderservice.infrastructure.client.ProductClient;
import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.model.OrderDetail;
import com.franchiseproject.orderservice.model.OrderStatusLog;
import com.franchiseproject.orderservice.repository.OrderRepository;
import com.franchiseproject.orderservice.repository.OrderStatusLogRepository;
import com.franchiseproject.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    //createOrder cho staff tại các POS//
    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        if (request.priceShip() == null || request.priceShip().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Shipping price must be >= 0");
        }


        Order order = Order.builder()
                .franchiseId(request.franchiseId())
                .customerId(request.customerId())
                .staffId(request.staffId())
                .paymentTransactionId(request.paymentTransactionId())
                .promotionId(request.promotionId())
                .address(request.address())
                .priceShip(request.priceShip())
                .typeOrder(request.typeOrder())
                .orderStatus(OrderStatus.CREATED)
                .build();

        BigDecimal totalItems = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (CreateOrderItemRequest item : request.items()) {

            if (item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            if (item.price() == null || item.price().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price must be >= 0");
            }

            BigDecimal itemTotal = item.price()
                    .multiply(BigDecimal.valueOf(item.quantity()));

            totalItems = totalItems.add(itemTotal);

            OrderDetail detail = OrderDetail.builder()
                    .productId(item.productId())
                    .productNameSnapshot(item.productName())
                    .priceSnapshot(item.price())
                    .cost(item.cost())
                    .quantity(item.quantity())
                    .order(order)
                    .build();

            orderDetails.add(detail);
        }

        BigDecimal finalTotal = totalItems.add(request.priceShip());

        order.setTotalDue(finalTotal);
        order.setOrderDetails(orderDetails);

        return orderRepository.save(order);

    }

    //checkoutOnline cho customer tại trang online//
    @Override
    @Transactional
    public UUID checkoutOnline(CheckoutRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("ORDER_001", "Order items cannot be empty");
        }

        BigDecimal totalProductCost = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderItemRequest item : request.getItems()) {

            if (item.getQuantity() <= 0) {
                throw new BusinessException("ORDER_002", "Quantity must be greater than 0");
            }

            ProductResponse product =
                    productClient.getProductById(item.getProductId());

            BigDecimal cost = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            totalProductCost = totalProductCost.add(cost);

            OrderDetail detail = OrderDetail.builder()
                    .productId(product.getId())
                    .productNameSnapshot(product.getName())
                    .quantity(item.getQuantity())
                    .priceSnapshot(product.getPrice())
                    .cost(cost)
                    .build();

            orderDetails.add(detail);
        }

        BigDecimal priceShip = calculateShipping(totalProductCost);
        BigDecimal totalDue = totalProductCost.add(priceShip);

        Order order = Order.builder()
                .franchiseId(request.getFranchiseId())
                .customerId(request.getCustomerId())
                .staffId(request.getStaffId())
                .paymentTransactionId(request.getPaymentTransactionId())
                .promotionId(request.getPromotionId())
                .address(request.getAddress())
                .typeOrder(TypeOrder.Online)
                .orderStatus(OrderStatus.WAITING_PAYMENT)
                .priceShip(priceShip)
                .totalDue(totalDue)
                .build();

        orderDetails.forEach(d -> d.setOrder(order));
        order.setOrderDetails(orderDetails);

        Order savedOrder = orderRepository.save(order);

        OrderStatusLog log = OrderStatusLog.builder()
                .statusId(UUID.randomUUID())
                .fromStatus("CONFIRMED")
                .toStatus(OrderStatus.WAITING_PAYMENT.name())
                .noteLog("Customer placed online order")
                .order(savedOrder)
                .build();

        orderStatusLogRepository.save(log);

        return savedOrder.getId();
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, UUID customerId) {


        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_003", "Order not found"));

        OrderStatus oldStatus = order.getOrderStatus();
        String status = oldStatus.name();

        if (!order.getCustomerId().equals(customerId)) {
            throw new BusinessException("ORDER_004", "You cannot cancel this order");
        }

        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == OrderStatus.CANCELLED) {
            return; // đã hủy rồi thì thôi
        }

        if (!currentStatus.canBeCancelledByCustomer()) {
            throw new BusinessException("ORDER_05", "Order cannot be cancelled");
        }
        if (currentStatus == OrderStatus.PAID) {
            order.setOrderStatus(OrderStatus.REFUNDED);
        } else {
            order.setOrderStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);

        orderStatusLogRepository.save(
                OrderStatusLog.builder()
                        .statusId(UUID.randomUUID())
                        .fromStatus(status)
                        .toStatus("CANCELLED")
                        .noteLog("Customer cancelled order")
                        .order(order)
                        .build()
        );
    }

    private BigDecimal calculateShipping(BigDecimal totalProductCost) {
        // hard-core nó mốt sửa sao
        // Nếu tổng tiền > 500k thì free ship
        if (totalProductCost.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(30000);
    }

}
