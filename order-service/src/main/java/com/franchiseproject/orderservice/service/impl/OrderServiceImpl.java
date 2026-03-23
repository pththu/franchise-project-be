package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.dto.*;
import com.franchiseproject.orderservice.dto.request.*;
import com.franchiseproject.orderservice.dto.response.ProductResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.enums.TypeOrder;
import com.franchiseproject.orderservice.client.ProductClient;
import com.franchiseproject.orderservice.client.CustomerClient;
import com.franchiseproject.orderservice.dto.response.CustomerResponse;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderDetailService orderDetailService;
    OrderMapper orderMapper;
    RedisTemplate<String, Object> redisTemplate;
    ProductClient productClient;
    CustomerClient customerClient;

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

    /// Client gửi request tạo order và trả lại orderId lên Client
    @Override
    @Transactional
    public UUID createOrder(CreateOrderRequest request) {
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
        return order.getId();
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
    }


    private Order buildOrder(CreateOrderRequest request) {
        return Order.builder()
                .franchiseId(request.getFranchiseId())
                .customerId(request.getCustomerId())
                .staffId(request.getStaffId())
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

    @Override
    @Transactional
    public void updatePaymentResult(com.franchiseproject.orderservice.dto.request.PaymentResultRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        order.setPaymentTransactionId(request.getPaymentTransactionId());
        if ("COMPLETED".equalsIgnoreCase(request.getStatus()) || "SUCCESS".equalsIgnoreCase(request.getStatus())) {
            order.setOrderStatus(OrderStatus.COMPLETED);
        } else if ("FAILED".equalsIgnoreCase(request.getStatus()) 
                || "CANCELLED".equalsIgnoreCase(request.getStatus())
                || "EXPIRED".equalsIgnoreCase(request.getStatus())) {
            order.setOrderStatus(OrderStatus.FAILED_PAYMENT);
        }
        
        orderRepository.save(order);
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


