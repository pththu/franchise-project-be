package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.dto.*;
import com.franchiseproject.orderservice.dto.request.*;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.client.PaymentClient;
import com.franchiseproject.orderservice.client.ProductClient;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import com.franchiseproject.orderservice.mapper.OrderMapper;
import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.model.OrderDetail;
import com.franchiseproject.orderservice.model.OrderStatusLog;
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
    public OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getOrderStatus();

        //  Không cho update nếu đã finalized
        if (currentStatus == OrderStatus.COMPLETED
                || currentStatus == OrderStatus.CANCELLED
                || currentStatus == OrderStatus.REFUNDED
                || currentStatus == OrderStatus.FAILED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
        }

        //  Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.ITEM_ORDER_NOT_NULL);
        }

        //  Validate shipping price
        BigDecimal shipping = request.getPriceShip() != null
                ? request.getPriceShip()
                : BigDecimal.ZERO;

        if (shipping.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_SHIPPING_PRICE);
        }

        //  Lấy product thật từ product-service
        Map<UUID, ProductResponse> apiProducts =
                orderDetailService.fetchProductsForUpdate(request.getItems());

        //  Xóa detail cũ (đảm bảo có orphanRemoval = true)
        order.getOrderDetails().clear();

        List<OrderDetail> newDetails = new ArrayList<>();

        for (UpdateOrderItemRequest item : request.getItems()) {

            ProductResponse product = apiProducts.get(item.getProductId());

            if (product == null) {
                throw new AppException(ErrorCode.MISSING_PRODUCTS);
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            OrderDetail detail = OrderDetail.builder()
                    .productId(product.getId())
                    .productNameSnapshot(product.getName())
                    .priceSnapshot(product.getPrice()) // không tin client
                    .quantity(item.getQuantity())
                    .order(order)
                    .build();

            newDetails.add(detail);
        }

        //  Tính lại totalItems
        BigDecimal totalItems = newDetails.stream()
                .map(d -> d.getPriceSnapshot()
                        .multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //  Tính lại discount
        BigDecimal discount = productClient.validateAndCalculate(
                order.getCustomerId(),
                order.getPromotionId(),
                totalItems
        );

        BigDecimal finalTotal = totalItems
                .add(shipping)
                .subtract(discount);

        //  Update thông tin order
        order.setPriceShip(shipping);
        order.setTotalDue(finalTotal);
        order.setOrderDetails(newDetails);

        if (request.getStaffId() != null) {
            order.setStaffId(request.getStaffId());
        }

        if (request.getAddress() != null) {
            order.setAddress(request.getAddress());
        }

        if (request.getTypeOrder() != null) {
            order.setTypeOrder(request.getTypeOrder());
        }

        Order updatedOrder = orderRepository.save(order);

        //  Log update
        orderStatusLogRepository.save(
                OrderStatusLog.builder()
                        .statusId(UUID.randomUUID())
                        .fromStatus(currentStatus.name())
                        .toStatus(currentStatus.name())
                        .noteLog("Order updated. New total: " + finalTotal)
                        .order(updatedOrder)
                        .build()
        );

        return orderMapper.toOrderResponse(updatedOrder);
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

    @Override
    @Transactional
    public PaymentResponse getOrder(UUID orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toPaymentResponse(order);
    }

}

