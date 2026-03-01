package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.dto.*;
import com.franchiseproject.orderservice.dto.request.*;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import com.franchiseproject.orderservice.exception.BusinessException;
import com.franchiseproject.orderservice.infrastructure.client.PaymentClient;
import com.franchiseproject.orderservice.infrastructure.client.ProductClient;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import com.franchiseproject.orderservice.mapper.OrderMapper;
import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.model.OrderDetail;
import com.franchiseproject.orderservice.model.OrderStatusLog;
import com.franchiseproject.orderservice.repository.OrderDetailRepository;
import com.franchiseproject.orderservice.repository.OrderRepository;
import com.franchiseproject.orderservice.repository.OrderStatusLogRepository;
import com.franchiseproject.orderservice.service.OrderDetailService;
import com.franchiseproject.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;

import static java.util.stream.Collectors.toList;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderStatusLogRepository orderStatusLogRepository;
    OrderDetailService orderDetailService;
    OrderMapper orderMapper;
    RedisTemplate<String, Object> redisTemplate;
    ProductClient productClient;
    PaymentClient paymentClient;

    @Override
    public List<OrderResponse> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }


    //createOrder cho staff tại các POS//
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = buildOrder(request);
        Map<UUID, ProductResponse> apiProducts = orderDetailService.fetchProducts(request.getItems());
        List<OrderDetail> details = orderDetailService.buildOrderDetails(request.getItems(), apiProducts, order);
        BigDecimal totalItems = orderDetailService.calculateTotal(details);

        BigDecimal discount = productClient.validateAndCalculate(request.getCustomerId(), request.getPromotionId(), totalItems);
        BigDecimal finalTotal = totalItems.add(request.getPriceShip().subtract(discount));//cần ý kiến nghiệp vụ về priceShip
        order.setTotalDue(finalTotal);
        order.setOrderDetails(details);
        order.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        orderRepository.save(order);

        PaymentResponse payment = paymentClient.createTransaction(order.getId(), order.getCustomerId(), finalTotal);    
        order.setPaymentTransactionId(payment.getPaymentTransactionId());

        Order savedOrder = orderRepository.save(order);
        OrderResponse orderResponse = orderMapper.toOrderResponse(savedOrder);
        orderResponse.setTransactionReference(payment.getTransactionReference());
        return orderResponse;
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, UUID customerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus oldStatus = order.getOrderStatus();
        String status = oldStatus.name();

        if (!order.getCustomerId().equals(customerId)) {
            throw new AppException(ErrorCode.WRONG_CUSTOMER_ID);
        }

        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == OrderStatus.CANCELLED) {
            return; // đã hủy rồi thì thôi
        }

        if (!currentStatus.canBeCancelledByCustomer()) {
            throw new AppException(ErrorCode.NO_CANCEL);
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

//    @KafkaListener(...)
//    public void handlePaymentSuccess(PaymentSuccessEvent event) {
//        Order order = orderRepository.findById(event.getOrderId());
//        order.setOrderStatus(OrderStatus.PAID);
//    }

    private Order buildOrder(CreateOrderRequest request) {
        return Order.builder()
                .franchiseId(request.getFranchiseId())
                .customerId(request.getCustomerId())
                .staffId(request.getStaffId())
                .paymentTransactionId(request.getPaymentTransactionId())
                .promotionId(request.getPromotionId())
                .address(request.getAddress())
                .priceShip(request.getPriceShip())
                .typeOrder(request.getTypeOrder())
                .orderStatus(OrderStatus.CREATED)
                .build();
    }


    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderStatus() == OrderStatus.COMPLETED
                || order.getOrderStatus() == OrderStatus.CANCELLED
                || order.getOrderStatus() == OrderStatus.REFUNDED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
        }
        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getOrderByCustomerId(UUID customerId) {
        List<Order> o = orderRepository.findAllByCustomerId(customerId);
        return o.stream().map(orderMapper::toOrderResponse).toList();
    }

    @Override
    @Transactional
    public Order updateOrder(UUID orderId, UpdateOrderRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra trạng thái order có thể cập nhật không
        OrderStatus currentStatus = order.getOrderStatus();
        if (currentStatus == OrderStatus.COMPLETED ||
                currentStatus == OrderStatus.CANCELLED ||
                currentStatus == OrderStatus.FAILED ||
                currentStatus == OrderStatus.REFUNDED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
        }

        // Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {

        }

        // Validate shipping price
        if (request.getPriceShip() == null || request.getPriceShip().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Shipping price must be >= 0");
        }

        // Cập nhật thông tin order
        if (request.getStaffId() != null) {
            order.setStaffId(request.getStaffId());
        }
        if (request.getPaymentTransactionId() != null) {
            order.setPaymentTransactionId(request.getPaymentTransactionId());
        }
        if (request.getPromotionId() != null) {
            order.setPromotionId(request.getPromotionId());
        }
        if (request.getAddress() != null) {
            order.setAddress(request.getAddress());
        }
        if (request.getTypeOrder() != null) {
            order.setTypeOrder(request.getTypeOrder());
        }

        order.setPriceShip(request.getPriceShip());

        // Xóa các order details cũ
        order.getOrderDetails().clear();

        // Tạo order details mới
        BigDecimal totalItems = BigDecimal.ZERO;
        List<OrderDetail> newOrderDetails = new ArrayList<>();

        for (UpdateOrderItemRequest item : request.getItems()) {

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price must be >= 0");
            }

            BigDecimal itemTotal = item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            totalItems = totalItems.add(itemTotal);

            OrderDetail detail = OrderDetail.builder()
                    .productId(item.getProductId())
                    .productNameSnapshot(item.getProductName())
                    .priceSnapshot(item.getPrice())
                    .cost(item.getCost())
                    .quantity(item.getQuantity())
                    .order(order)
                    .build();

            newOrderDetails.add(detail);
        }

        // Cập nhật tổng tiền
        BigDecimal finalTotal = totalItems.add(request.getPriceShip());
        order.setTotalDue(finalTotal);
        order.setOrderDetails(newOrderDetails);

        // Lưu order đã cập nhật
        Order updatedOrder = orderRepository.save(order);

        // Log việc cập nhật order
        OrderStatusLog log = OrderStatusLog.builder()
                .statusId(UUID.randomUUID())
                .fromStatus(currentStatus.name())
                .toStatus(currentStatus.name())
                .noteLog("Order updated - total due changed to " + finalTotal)
                .order(updatedOrder)
                .build();

        orderStatusLogRepository.save(log);

        return updatedOrder;
    }

    @Override
    public void addAddressOnlineOrder(AddAddressRequest request) {
        String key = "online_order:" + request.getCustomerId();
        redisTemplate.opsForValue().set(key, request.getAddress());
    }

    @Override
    public String getAddressOnlineOrder(UUID customerId) {
        String key = "online_order:" + customerId;
        Object address = redisTemplate.opsForValue().get(key);
        return address != null ? address.toString() : null;
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
