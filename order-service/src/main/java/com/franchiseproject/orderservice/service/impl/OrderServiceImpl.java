package com.franchiseproject.orderservice.service.impl;

import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.response.OrderResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import com.franchiseproject.orderservice.mapper.OrderMapper;
import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.repository.OrderRepository;
import com.franchiseproject.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderMapper  orderMapper;
    RedisTemplate<String, Object> redisTemplate;
    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderStatus() == OrderStatus.COMPLETED
                || order.getOrderStatus() == OrderStatus.CANCELED
                || order.getOrderStatus() == OrderStatus.REFUNDED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_FINALIZED);
        }
        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getOrderByCustomerId(UUID customerId) {
       List<Order> o =  orderRepository.findAllByCustomerId(customerId);
       return o.stream().map(orderMapper::toOrderResponse).toList();
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
}
