package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.client.*;
import com.franchiseproject.orderservice.dto.*;
import com.franchiseproject.orderservice.dto.request.*;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.dto.response.PromotionDiscountResponse;
import com.franchiseproject.orderservice.enums.DiscountType;
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
import java.math.RoundingMode;
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
    ProductClient productClient;

    @Override
    public List<OrderResponse> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponseWithCustomer)
                .toList();
    }

    @Override
    public List<OrderResponse> searchOrderById(String keyword) {
        List<UUID> customerIds = customerClient.searchCustomerIdsByKeyword(keyword, null);
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

        List<OrderResponse> responseList = orders.stream()
                .map(order -> {
                    OrderResponse res = orderMapper.toOrderResponse(order);
                    var customer = customerMap.get(order.getCustomerId());
                    res.setCustomerName(customer != null ? customer.getFullName() : "Guest");
                    return res;
                })
                .toList();

        populateProductImagesForList(responseList);
        return responseList;
    }

    /// Client gửi request tạo order
    @Override
    public PaymentQRResponse createOrder(CreateOrderRequest request) {
        Order order = buildOrder(request);
        Map<UUID, ProductResponse> apiProducts = orderDetailService.fetchProducts(request.getItems());
        List<OrderDetail> details = orderDetailService.buildOrderDetails(request.getItems(), apiProducts, order);
        order.setOrderDetails(details);
        BigDecimal totalItems = orderDetailService.calculateTotal(details);
        order.setTotalDue(totalItems);//tổng hóa đơn khi chưa trừ
        orderRepository.save(order);
        return handleReserve(order, request, totalItems);
    }

    /// call promotion-service và loyalty-service để giữ chỗ
    @Override
    public PaymentQRResponse handleReserve(Order order, CreateOrderRequest request, BigDecimal totalItems) {
        boolean usedLoyalty = false;
        boolean usedPromotion = false;
        try {
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal maxDiscountValue = BigDecimal.ZERO;
            DiscountType discountType = null;
            if (request.getPoint() != null && request.getPoint() > 0) {
                discount = loyaltyClient.apiLoyaltyReserve(request.getCustomerId(), request.getFranchiseId(), order.getId(), request.getPoint());
                discountType = DiscountType.FIXED;
                maxDiscountValue = discount;
                usedLoyalty = true;
            } else if (request.getPromotionId() != null) {
                PromotionDiscountResponse promotion = promotionClient.apiPromotionReserve(request.getPromotionId(),
                        request.getFranchiseId(), request.getCustomerId(), order.getId(), totalItems);
                discount = promotion.getDiscountValue();
                discountType = promotion.getDiscountType();
                maxDiscountValue = promotion.getMaxDiscountValue();
                usedPromotion = discount.compareTo(BigDecimal.ZERO) > 0;
            }
            BigDecimal finalTotal = calculateOrder(totalItems, request.getDistance(), discount, discountType, maxDiscountValue);
            order.setTotalDue(finalTotal);
            order.setOrderStatus(OrderStatus.WAITING_FOR_CONFIRMATION);
            orderRepository.save(order);// save lần 2 sau khi set giá cả các thứ.
            if (request.getTypeOrder() != null && "Online".equalsIgnoreCase(request.getTypeOrder().name())) {
                inventoryClient.notifyNewOrder(order.getFranchiseId());
            }

            // Reserve inventory immediately
            List<InventoryReserveRequest.InventoryItemRequest> items = order.getOrderDetails().stream()
                    .map(d -> InventoryReserveRequest.InventoryItemRequest.builder()
                            .productVariantId(d.getProductId())
                            .quantity(d.getQuantity())
                            .build())
                    .toList();
            inventoryClient.reserveStock(InventoryReserveRequest.builder()
                    .locationId(order.getFranchiseId())
                    .items(items)
                    .build());

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
                if (res.getPaymentTransactionId() != null) {
                    order.setPaymentTransactionId(res.getPaymentTransactionId());
                }
            }
            if (res == null) {
                res = PaymentQRResponse.builder().build();
            }
            // If POS and no payment URL, it's a cash/synchronous payment -> Complete it now!
            if (order.getTypeOrder() == TypeOrder.POS && (res.getPaymentUrl() == null || res.getPaymentUrl().isEmpty())) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
                finalizeSuccessfulOrder(order);
            } else {
                orderRepository.save(order);
            }
            res.setOrderId(order.getId());
            return res;
        } catch (Exception e) {
            log.error("Payment init failed for order {}", order.getId(), e);
            if (order.getTypeOrder() == TypeOrder.POS) {
                log.info("POS Order {} initialization failed. Deleting permanently to maintain success-only policy.", order.getId());
                // Permanent cleanup
                safeRollback(request.getCustomerId(), request.getFranchiseId(), order.getId(), request.getPoint(), false, false);
                orderRepository.delete(order);
            } else {
                log.info("Online Payment init failed for order {}. Deleting permanently.", order.getId());
                safeRollback(request.getCustomerId(), request.getFranchiseId(), order.getId(), request.getPoint(), false, false);
                deleteOrderPermanently(order.getId());
            }
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
            order.setOrderStatus(order.getTypeOrder() == TypeOrder.POS ? OrderStatus.COMPLETED : OrderStatus.WAITING_FOR_CONFIRMATION);
            orderRepository.save(order);
            finalizeSuccessfulOrder(order);
        } else if (result.getStatus() == StatusTransaction.FAILED
                || result.getStatus() == StatusTransaction.CANCELLED
                || result.getStatus() == StatusTransaction.EXPIRED) {

            // POS Success-Only policy: If POS and not success, rollback and DELETE
            if (order.getTypeOrder() == TypeOrder.POS) {
                log.info("POS Payment result NOT SUCCESS for order {}. Rolling back and deleting.", order.getId());

                // Rollback loyalty and promotion if applicable
                // Note: We need points for rollback. Order doesn't store points directly, but we can check the request if we had it.
                // However, safeRollback is designed to handle this.
                // We'll use a simplified rollback or fetch relevant data.

                // For now, trigger cancel notifications to other services
                inventoryClient.notifyOrderStatus(order.getId(), "CANCELLED", order.getFranchiseId());

                // Actually, the most reliable way to rollback is to use the order details
                // since we are about to delete it.
                promotionClient.apiPromotionTraceBack(order.getId(), OrderStatus.FAILED_ORDER);

                deleteOrderPermanently(order.getId());
            } else {
                // Online order (Customer): if payment failed, rollback, DELETE transaction, and DELETE order
                log.info("Online Payment result NOT SUCCESS for order {}. Rolling back, deleting transaction, and deleting order.", order.getId());
                promotionClient.apiPromotionTraceBack(order.getId(), OrderStatus.FAILED_ORDER);
                releaseInventory(order);
                inventoryClient.notifyOrderStatus(order.getId(), "FAILED_ORDER", order.getFranchiseId());
                paymentClient.deleteTransactionByOrderId(order.getId());
                deleteOrderPermanently(order.getId());
            }
        }
    }

    private void finalizeSuccessfulOrder(Order order) {
        // Trigger inventory commit if POS. Online orders commit at SHIPPING status.
        if (order.getTypeOrder() == TypeOrder.POS) {
            commitInventory(order);
        }
        if (order.getCustomerId() != null) {
            try {
                CustomerResponse customer = customerClient.getCustomerById(order.getCustomerId());
                if (customer != null && customer.getUserId() != null) {
                    log.info("Loyalty: Earning points for user {} with amount {}", customer.getUserId(), order.getTotalDue());
                    loyaltyClient.apiLoyaltyEarn(customer.getUserId(), order.getFranchiseId(), order.getTotalDue().doubleValue());
                } else {
                    log.warn("Loyalty: Could not earn points for order {}. Customer info or userId missing.", order.getId());
                }
            } catch (Exception e) {
                log.warn("Failed to earn loyalty for order {}: {}", order.getId(), e.getMessage());
            }
        }
        promotionClient.apiPromotionTraceBack(order.getId(), order.getOrderStatus());
        inventoryClient.notifyOrderStatus(order.getId(), order.getOrderStatus().name(), order.getFranchiseId());
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
        // If cancellation happens, we move to CANCELLED. 
        // Note: For online orders, they were confirmed (WAITING_FOR_CONFIRMATION) or preparing.
        order.setOrderStatus(OrderStatus.CANCELLED);
        releaseInventory(order);
        inventoryClient.notifyOrderStatus(order.getId(), order.getOrderStatus().name(), order.getFranchiseId());
        inventoryClient.notifyNewOrder(order.getFranchiseId());
    }


    private Order buildOrder(CreateOrderRequest request) {
        return Order.builder()
                .franchiseId(request.getFranchiseId())
                .customerId(request.getCustomerId())
                .staffId(request.getStaffId())
                .address(request.getAddress())
                .priceShip(BigDecimal.valueOf(request.getDistance()).multiply(BigDecimal.valueOf(20000))) //1km = 20000vnd
                .typeOrder(request.getTypeOrder())
                .orderStatus(OrderStatus.WAITING_FOR_CONFIRMATION)
                .build();
    }


    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus, UUID staffId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderStatus() == OrderStatus.COMPLETED
                || order.getOrderStatus() == OrderStatus.CANCELLED
                || order.getOrderStatus() == OrderStatus.REFUNDED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
        }
        if (newStatus == OrderStatus.PREPARING) {
            // Inventory is already reserved at order creation (handleReserve)
            log.info("Order {} is now PREPARING. Inventory was already reserved.", orderId);
        } else if (newStatus == OrderStatus.SHIPPING) {
            commitInventory(order);
        } else if (newStatus == OrderStatus.COMPLETED) {
            // Inventory is committed at SHIPPING for Online or at creation for POS.
            // No additional commit needed here to avoid double-deduction.
            log.info("Order {} is now COMPLETED.", orderId);
        } else if (newStatus == OrderStatus.CANCELLED) {
            releaseInventory(order);
        }

        order.setOrderStatus(newStatus);
        if (staffId != null) {
            order.setStaffId(staffId);
        }
        orderRepository.save(order);
        inventoryClient.notifyOrderStatus(orderId, newStatus.name(), order.getFranchiseId());
        inventoryClient.notifyNewOrder(order.getFranchiseId());
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
        List<UUID> customerIds = customerClient.searchCustomerIdsByKeyword(keyword, franchiseId);
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

        Page<OrderResponse> responsePage = orders.map(order -> {
            OrderResponse res = orderMapper.toOrderResponse(order);
            var customer = customerMap.get(order.getCustomerId());
            res.setCustomerName(customer != null ? customer.getFullName() : "Guest");
            return res;
        });

        populateProductImagesForList(responsePage.getContent());
        return responsePage;
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
    private BigDecimal calculateOrder(BigDecimal totalItems, Long distance, BigDecimal discount, DiscountType discountType, BigDecimal maxDiscountValue) {
        if (totalItems == null || totalItems.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal priceShip = BigDecimal.valueOf(distance).multiply(BigDecimal.valueOf(20000));
        BigDecimal finalAmount = totalItems.add(priceShip);

        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return finalAmount;
        }
        if (DiscountType.PERCENT.equals(discountType)) {
            discountAmount = totalItems
                    .multiply(discount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (maxDiscountValue != null) {
                discountAmount = discountAmount.min(maxDiscountValue);
            }
        } else if (DiscountType.FIXED.equals(discountType)) {
            discountAmount = discount;
        }
        // chống âm tiền
        BigDecimal result = finalAmount.subtract(discountAmount);
        return result.max(BigDecimal.ZERO);
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

        Order order = orderRepository.findById(orderId).orElse(null);
        releaseInventory(order);
    }

    private void commitInventory(Order order) {
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

        // Stock was already reserved at order creation or status PREPARING
        // So we just call commitStock
        inventoryClient.commitStock(reserveReq);
    }

    private void releaseInventory(Order order) {
        if (order == null || order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) return;

        List<InventoryReserveRequest.InventoryItemRequest> items = order.getOrderDetails().stream()
                .map(d -> InventoryReserveRequest.InventoryItemRequest.builder()
                        .productVariantId(d.getProductId())
                        .quantity(d.getQuantity())
                        .build())
                .toList();

        try {
            inventoryClient.releaseStock(InventoryReserveRequest.builder()
                    .locationId(order.getFranchiseId())
                    .items(items)
                    .build());
            log.info("Inventory released for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to release inventory for order: {}", order.getId(), e);
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
        populateProductImages(res);
        return res;
    }

    private void populateProductImages(OrderResponse res) {
        if (res == null || res.getOrderDetails() == null || res.getOrderDetails().isEmpty()) return;
        List<UUID> variantIds = res.getOrderDetails().stream()
                .map(OrderItemResponse::getProductId)
                .filter(Objects::nonNull)
                .distinct().toList();
        if (variantIds.isEmpty()) return;
        try {
            Map<UUID, ProductResponse> products = productClient.getProductsByIds(variantIds);
            res.getOrderDetails().forEach(item -> {
                ProductResponse p = products.get(item.getProductId());
                if (p != null) item.setProductImageUrl(p.getImageUrl());
            });
        } catch (Exception e) {
            log.warn("Failed to fetch product images for order detail: {}", e.getMessage());
        }
    }

    private void populateProductImagesForList(List<OrderResponse> list) {
        if (list == null || list.isEmpty()) return;
        List<UUID> allVariantIds = list.stream()
                .filter(res -> res.getOrderDetails() != null)
                .flatMap(res -> res.getOrderDetails().stream())
                .map(OrderItemResponse::getProductId)
                .filter(Objects::nonNull)
                .distinct().toList();
        if (allVariantIds.isEmpty()) return;
        try {
            Map<UUID, ProductResponse> products = productClient.getProductsByIds(allVariantIds);
            list.forEach(res -> {
                if (res.getOrderDetails() != null) {
                    res.getOrderDetails().forEach(item -> {
                        ProductResponse p = products.get(item.getProductId());
                        if (p != null) item.setProductImageUrl(p.getImageUrl());
                    });
                }
            });
        } catch (Exception e) {
            log.warn("Failed to fetch product images for order list: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteOrderPermanently(UUID orderId) {
        // Also delete transaction in payment-service
        try {
            paymentClient.deleteTransactionByOrderId(orderId);
        } catch (Exception e) {
            log.warn("Failed to notify payment-service to delete transaction for order {}: {}", orderId, e.getMessage());
        }

        orderRepository.deleteById(orderId);
        log.info("Order {} deleted permanently.", orderId);
    }
}


