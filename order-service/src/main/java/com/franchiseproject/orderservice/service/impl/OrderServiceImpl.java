package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.client.*;
import com.franchiseproject.orderservice.dto.*;
import com.franchiseproject.orderservice.dto.request.*;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.dto.response.PromotionDiscountResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import com.franchiseproject.orderservice.dto.response.CustomerResponse;
import com.franchiseproject.orderservice.enums.StatusTransaction;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import com.franchiseproject.orderservice.mapper.OrderMapper;
import com.franchiseproject.orderservice.entity.Order;
import com.franchiseproject.orderservice.entity.OrderDetail;
import com.franchiseproject.orderservice.repository.OrderRepository;
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
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderDetailService orderDetailService;
    OrderMapper orderMapper;
    RedisTemplate<String, Object> redisTemplate;
    CustomerClient customerClient;
    PromotionClient promotionClient;
    LoyaltyClient loyaltyClient;
    PaymentClient paymentClient;
    InventoryClient inventoryClient;

    @Override
    public List<OrderResponse> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponseWithCustomer)
                .toList();
    }

    @Override
    public List<OrderResponse> searchOrderById(String keyword) {
        List<UUID> customerIds = customerClient.searchCustomerIdsByKeyword(keyword);
        List<Order> orders;
        if (customerIds.isEmpty()) {
            orders = orderRepository.searchOrderByIdLike(keyword);
        } else {
            orders = orderRepository.searchOrdersByCustomerIdsWithoutFranchise(keyword, customerIds);
        }

        List<UUID> activeCustomerIds = orders.stream()
                .map(Order::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, CustomerResponse> customerMap = customerClient.getCustomersByIds(activeCustomerIds);

        return orders.stream()
                .map(order -> {
                    OrderResponse res = orderMapper.toOrderResponse(order);
                    var customer = customerMap.get(order.getCustomerId());
                    res.setCustomerName(customer != null ? customer.getFullName() : "Guest");
                    return res;
                })
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
            if (request.getTypeOrder() != null && "Online".equalsIgnoreCase(request.getTypeOrder().name())) {
                inventoryClient.notifyNewOrder(order.getFranchiseId());
            }
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
            PaymentQRResponse res = paymentClient.createTransaction(order.getId(), request.getPaymentMethodId());
            if (res != null) {
                log.info("createTransaction returned paymentTransactionId: {}", res.getPaymentTransactionId());
            }
            if (res == null) {
                res = PaymentQRResponse.builder().build();
            } else if (res.getPaymentTransactionId() != null) {
                order.setPaymentTransactionId(res.getPaymentTransactionId());
                orderRepository.save(order);
            }
            res.setOrderId(order.getId());
            return res;
        } catch (Exception e) {
            log.error("Payment init failed", e);
            order.setOrderStatus(OrderStatus.FAILED_PAYMENT);
            orderRepository.save(order);
            throw new AppException(ErrorCode.PAYMENT_INIT_FAILED);
        }
    }

    @Override
    @Transactional
    public void handlePaymentResult(PaymentResultRequest result) {
        Order order = orderRepository.findById(result.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setPaymentTransactionId(result.getPaymentTransactionId());
        if (result.getStatus() == StatusTransaction.SUCCESS) {
            order.setOrderStatus(OrderStatus.PAID);
        } else if (result.getStatus() == StatusTransaction.FAILED
                || result.getStatus() == StatusTransaction.CANCELLED
                || result.getStatus() == StatusTransaction.EXPIRED) {
            order.setOrderStatus(OrderStatus.FAILED_PAYMENT);
        }
        orderRepository.save(order);
        inventoryClient.notifyOrderStatus(order.getId(), order.getOrderStatus().name());
        inventoryClient.notifyNewOrder(order.getFranchiseId());
    }


    @Override
    @Transactional
    public void cancelOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
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
        inventoryClient.notifyOrderStatus(order.getId(), order.getOrderStatus().name());
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
        if (newStatus == OrderStatus.PREPARING) {
            List<InventoryReserveRequest.InventoryItemRequest> items = order.getOrderDetails().stream()
                    .map(d -> InventoryReserveRequest.InventoryItemRequest.builder()
                            .productVariantId(d.getProductId())
                            .quantity(d.getQuantity())
                            .build())
                    .toList();
            InventoryReserveRequest reserveReq = InventoryReserveRequest.builder()
                    .locationId(order.getFranchiseId())
                    .items(items)
                    .build();
            inventoryClient.reserveStock(reserveReq);
        } else if (newStatus == OrderStatus.COMPLETED) {
            List<InventoryReserveRequest.InventoryItemRequest> items = order.getOrderDetails().stream()
                    .map(d -> InventoryReserveRequest.InventoryItemRequest.builder()
                            .productVariantId(d.getProductId())
                            .quantity(d.getQuantity())
                            .build())
                    .toList();
            InventoryReserveRequest reserveReq = InventoryReserveRequest.builder()
                    .locationId(order.getFranchiseId())
                    .items(items)
                    .build();
            inventoryClient.commitStock(reserveReq);
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
        inventoryClient.notifyOrderStatus(orderId, newStatus.name());
    }

    @Override
    public List<OrderResponse> getOrderByCustomerId(UUID customerId) {
        List<Order> o = orderRepository.findAllByCustomerId(customerId);
        return o.stream().map(this::mapToOrderResponseWithCustomer).toList();
    }



    @Override
    public Page<OrderResponse> getOrdersByFranchiseAndFilters(
            UUID franchiseId,
            OrderStatus status,
            TypeOrder typeOrder,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<Order> orders = orderRepository.findByFranchiseIdAndFilters(franchiseId, status, typeOrder, pageable);

        List<UUID> customerIds = orders.getContent().stream()
                .map(Order::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, CustomerResponse> customerMap = customerClient.getCustomersByIds(customerIds);

        return orders.map(order -> {
            OrderResponse res = orderMapper.toOrderResponse(order);
            var customer = customerMap.get(order.getCustomerId());
            res.setCustomerName(customer != null ? customer.getFullName() : "Guest");
            return res;
        });
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
        List<UUID> customerIds = customerClient.searchCustomerIdsByKeyword(keyword);
        List<Order> orders;
        if (customerIds.isEmpty()) {
            orders = orderRepository.searchOrders(franchiseId, keyword);
        } else {
            orders = orderRepository.searchOrdersByCustomerIds(franchiseId, keyword, customerIds);
        }

        List<UUID> activeCustomerIds = orders.stream()
                .map(Order::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, CustomerResponse> customerMap = customerClient.getCustomersByIds(activeCustomerIds);

        return orders.stream()
                .map(order -> {
                    OrderResponse res = orderMapper.toOrderResponse(order);
                    var customer = customerMap.get(order.getCustomerId());
                    res.setCustomerName(customer != null ? customer.getFullName() : "Guest");
                    return res;
                })
                .toList();
    }


    @Override
    public Page<OrderResponse> getOrdersByFilters(
            OrderStatus status,
            TypeOrder typeOrder,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<Order> orders = orderRepository.findByFilters(status, typeOrder, pageable);

        List<UUID> customerIds = orders.getContent().stream()
                .map(Order::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, CustomerResponse> customerMap = customerClient.getCustomersByIds(customerIds);

        return orders.map(order -> {
            OrderResponse res = orderMapper.toOrderResponse(order);
            var customer = customerMap.get(order.getCustomerId());
            res.setCustomerName(customer != null ? customer.getFullName() : "Guest");
            return res;
        });
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
        return orders.map(this::mapToOrderResponseWithCustomer);
    }

    @Override
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return mapToOrderResponseWithCustomer(order);
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

    private OrderResponse mapToOrderResponseWithCustomer(Order order) {
        OrderResponse res = orderMapper.toOrderResponse(order);
        if (order.getCustomerId() != null) {
            CustomerResponse c = customerClient.getCustomerById(order.getCustomerId());
            if (c != null) {
                res.setCustomerName(c.getFullName());
            }
        }
        return res;
    }
}


