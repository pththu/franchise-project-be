package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.client.LoyaltyClient;
import com.franchiseproject.orderservice.client.PromotionClient;
import com.franchiseproject.orderservice.dto.*;
import com.franchiseproject.orderservice.dto.request.*;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.dto.response.PromotionDiscountResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.client.PaymentClient;
import com.franchiseproject.orderservice.client.ProductClient;
import com.franchiseproject.orderservice.enums.StatusTransaction;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import com.franchiseproject.orderservice.mapper.OrderMapper;
import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.entity.OrderDetail;
import com.franchiseproject.orderservice.entity.OrderStatusLog;
import com.franchiseproject.orderservice.repository.OrderRepository;
import com.franchiseproject.orderservice.repository.OrderStatusLogRepository;
import com.franchiseproject.orderservice.service.OrderDetailService;
import com.franchiseproject.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderStatusLogRepository orderStatusLogRepository;
    OrderDetailService orderDetailService;
    OrderMapper orderMapper;
    RedisTemplate<String, Object> redisTemplate;
    PromotionClient promotionClient;
    LoyaltyClient loyaltyClient;
    PaymentClient paymentClient;

    @Override
    public List<OrderResponse> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    /// Client gửi request tạo order
    @Override
    public PaymentQRResponse createOrder(CreateOrderRequest request) {
        Order order = buildOrder(request);
        try {
            Map<UUID, ProductResponse> apiProducts = orderDetailService.fetchProducts(request.getItems());
            List<OrderDetail> details = orderDetailService.buildOrderDetails(request.getItems(), apiProducts, order);
            order.setOrderDetails(details);
            BigDecimal totalItems = orderDetailService.calculateTotal(details);
            order.setTotalDue(totalItems);//tổng hóa đơn khi chưa trừ
            orderRepository.save(order); // save lần một lấy orderId
            return handleReserve(order, request, totalItems);
        } catch (Exception e) {
            log.error("Create order failed at initial step", e);
            order.setOrderStatus(OrderStatus.FAILED_ORDER);
            orderRepository.save(order);
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /// call promotion-service và loyalty-service để giữ chỗ
    @Override
    public PaymentQRResponse handleReserve(Order order, CreateOrderRequest request, BigDecimal totalItems) {
        boolean usedLoyalty = false;
        boolean usedPromotion = false;
        try {
            BigDecimal discount = BigDecimal.ZERO;
            if (request.getPoint() != null) {
                discount = loyaltyClient.apiLoyaltyReserve(request.getCustomerId(), request.getPoint());
                usedLoyalty = discount.compareTo(BigDecimal.ZERO) > 0;
            } else if (request.getPromotionId() != null) {
                PromotionDiscountResponse promotion = promotionClient.apiPromotionReserve(request.getPromotionId(),
                        request.getCustomerId(), order.getId(), totalItems);
                discount = promotion.getDiscountValue();
                usedPromotion = discount.compareTo(BigDecimal.ZERO) > 0;
            }
            BigDecimal finalTotal = calculateOrder(totalItems, request.getDistance(), discount);
            order.setTotalDue(finalTotal);
            order.setOrderStatus(OrderStatus.WAITING_PAYMENT);
            orderRepository.save(order);// save lần 2 sau khi set giá cả các thứ.
            return handlePayment(order, request);
        } catch (AppException a) {
            log.error("Create order failed", a);
            safeRollback(request.getCustomerId(), request.getFranchiseId(),
                    order.getId(), request.getPoint(), usedPromotion, usedLoyalty);
            order.setOrderStatus(OrderStatus.FAILED_ORDER);
            throw a;
        }
    }

    @Override
    public PaymentQRResponse handlePayment(Order order, CreateOrderRequest request) {
        try {
            return paymentClient.createTransaction(order.getId(), request.getPaymentMethodId());
        } catch (Exception e) {
            log.error("Payment init failed", e);
            order.setOrderStatus(OrderStatus.FAILED_PAYMENT);
            orderRepository.save(order);
            return null;
        }
    }

    @Override
    @Transactional
    public void handlePaymentResult(PaymentResultRequest result) {
        Order order = orderRepository.findById(result.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));;
        order.setPaymentTransactionId(result.getPaymentTransactionId());
        if (result.getStatus() == StatusTransaction.SUCCESS) {
            order.setOrderStatus(OrderStatus.PAID);
        } else if (result.getStatus() == StatusTransaction.FAILED
                || result.getStatus() == StatusTransaction.CANCELLED
                || result.getStatus() == StatusTransaction.EXPIRED) {
            order.setOrderStatus(OrderStatus.FAILED_ORDER);
        }
        orderRepository.save(order);
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


    private Order buildOrder(CreateOrderRequest request) {
        return Order.builder()
                .franchiseId(request.getFranchiseId())
                .customerId(request.getCustomerId())
                .staffId(request.getStaffId())
                .address(request.getAddress())
                .priceShip(BigDecimal.valueOf(request.getDistance()).multiply(BigDecimal.valueOf(20000))) //1km = 20000vnd
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

//    @Override
//    @Transactional
//    public OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request) {
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        OrderStatus currentStatus = order.getOrderStatus();
//
//        //  Không cho update nếu đã finalized
//        if (currentStatus == OrderStatus.COMPLETED
//                || currentStatus == OrderStatus.CANCELLED
//                || currentStatus == OrderStatus.REFUNDED
//                || currentStatus == OrderStatus.FAILED_ORDER) {
//            throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
//        }
//
//        //  Validate items
//        if (request.getItems() == null || request.getItems().isEmpty()) {
//            throw new AppException(ErrorCode.ITEM_ORDER_NOT_NULL);
//        }
//
//        //  Validate shipping price
//        BigDecimal shipping = request.getPriceShip() != null
//                ? request.getPriceShip()
//                : BigDecimal.ZERO;
//
//        if (shipping.compareTo(BigDecimal.ZERO) < 0) {
//            throw new AppException(ErrorCode.INVALID_SHIPPING_PRICE);
//        }
//
//        //  Lấy product thật từ product-service
//        Map<UUID, ProductResponse> apiProducts =
//                orderDetailService.fetchProductsForUpdate(request.getItems());
//
//        //  Xóa detail cũ (đảm bảo có orphanRemoval = true)
//        order.getOrderDetails().clear();
//
//        List<OrderDetail> newDetails = new ArrayList<>();
//
//        for (UpdateOrderItemRequest item : request.getItems()) {
//
//            ProductResponse product = apiProducts.get(item.getProductId());
//
//            if (product == null) {
//                throw new AppException(ErrorCode.MISSING_PRODUCTS);
//            }
//
//            if (item.getQuantity() == null || item.getQuantity() <= 0) {
//                throw new AppException(ErrorCode.OUT_OF_STOCK);
//            }
//
//            OrderDetail detail = OrderDetail.builder()
//                    .productId(product.getId())
//                    .productNameSnapshot(product.getName())
//                    .priceSnapshot(product.getPrice()) // không tin client
//                    .quantity(item.getQuantity())
//                    .order(order)
//                    .build();
//
//            newDetails.add(detail);
//        }
//
//        //  Tính lại totalItems
//        BigDecimal totalItems = newDetails.stream()
//                .map(d -> d.getPriceSnapshot()
//                        .multiply(BigDecimal.valueOf(d.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        //  Tính lại discount
//        BigDecimal discount = promotionClient.apiPromotionGetValue(
//                order.getCustomerId(),
//                order.get,
//                totalItems
//        );
//
//        BigDecimal finalTotal = totalItems
//                .add(shipping)
//                .subtract(discount);
//
//        //  Update thông tin order
//        order.setPriceShip(shipping);
//        order.setTotalDue(finalTotal);
//        order.setOrderDetails(newDetails);
//
//        if (request.getStaffId() != null) {
//            order.setStaffId(request.getStaffId());
//        }
//
//        if (request.getAddress() != null) {
//            order.setAddress(request.getAddress());
//        }
//
//        if (request.getTypeOrder() != null) {
//            order.setTypeOrder(request.getTypeOrder());
//        }
//
//        Order updatedOrder = orderRepository.save(order);
//
//        //  Log update
//        orderStatusLogRepository.save(
//                OrderStatusLog.builder()
//                        .statusId(UUID.randomUUID())
//                        .fromStatus(currentStatus.name())
//                        .toStatus(currentStatus.name())
//                        .noteLog("Order updated. New total: " + finalTotal)
//                        .order(updatedOrder)
//                        .build()
//        );
//
//        return orderMapper.toOrderResponse(updatedOrder);
//    }

    @Override
    public Page<OrderResponse> getOrdersByFranchiseAndStatus(
            UUID franchiseId,
            OrderStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createAt")
        );
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByFranchiseIdAndOrderStatus(franchiseId, status, pageable);
        } else {
            orders = orderRepository.findByFranchiseId(franchiseId, pageable);
        }

        return orders.map(orderMapper::toOrderResponse);
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

        order.setStaffId(staffId);

        orderRepository.save(order);
    }

    @Override
    public void markSpecial(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

//        order.setIsSpecial(true);

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
    public List<OrderResponse> searchOrders(UUID franchiseId, String keyword) {
        List<Order> orders = orderRepository.searchOrders(franchiseId, keyword);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(
            OrderStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createAt")
        );
        Page<Order> orders;
        if (status == null) {
            orders = orderRepository.findAll(pageable);
        } else {
            orders = orderRepository.findByOrderStatus(status, pageable);
        }
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public List<OrderResponse> searchOrderById(String keyword) {

        List<Order> orders = orderRepository.searchOrderByIdLike(keyword);

        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public Page<OrderResponse> getOrdersByCustomerIdAndStatus(
            UUID customerId,
            OrderStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createAt")
        );
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByCustomerIdAndOrderStatus(
                    customerId,
                    status,
                    pageable
            );
        } else {
            orders = orderRepository.findByCustomerId(
                    customerId,
                    pageable
            );
        }
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public PaymentResponse getOrder(UUID orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toPaymentResponse(order);
    }

    /// Tính số tiền cần trả
    private BigDecimal calculateOrder(BigDecimal totalItems, Long distance, BigDecimal discount) {
        BigDecimal priceShip = BigDecimal.valueOf(distance).multiply(BigDecimal.valueOf(20000));
        return totalItems.add(priceShip).subtract(discount);
    }

    /// Dùng để traceback
    private void safeRollback(UUID customerId, UUID franchiseId, UUID orderId, Integer pointsToRefund,
                              boolean usedPromotion, boolean usedLoyalty) {
        try {
            if (usedPromotion) {
                promotionClient.apiPromotionTraceBack(orderId, OrderStatus.FAILED_ORDER);
            }
        } catch (Exception e) {
            log.error("Promotion rollback failed", e);
        }

        try {
            if (usedLoyalty) {
                loyaltyClient.apiLoyaltyTraceBackPoints(customerId, franchiseId, orderId, pointsToRefund);
            }
        } catch (Exception e) {
            log.error("Loyalty rollback failed", e);
        }

    }
}


