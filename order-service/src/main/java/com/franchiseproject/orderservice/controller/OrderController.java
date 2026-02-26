package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.dto.CheckoutRequest;
import com.franchiseproject.orderservice.dto.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.OrderItemResponse;
import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.service.OrderDetailService;
import com.franchiseproject.orderservice.service.OrderService;
import com.franchiseproject.orderservice.service.OrderStatusLogService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {
    OrderService orderService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrder() {
        Map<String, Object> response = new HashMap<>();

        List<Order> orders = orderService.getAll();
        response.put("message", "Get All Orders");
        response.put("data", orders);

        return ResponseEntity.ok(response);
    }


    //createOrder cho staff tại các POS
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request
    ) {
        Order order = orderService.createOrder(request);
        OrderResponse response = mapToResponse(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items = order.getOrderDetails()
                .stream()
                .map(detail -> OrderItemResponse.builder()
                        .productId(detail.getProductId())
                        .productName(detail.getProductNameSnapshot())
                        .price(detail.getPriceSnapshot())
                        .quantity(detail.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .franchiseId(order.getFranchiseId())
                .customerId(order.getCustomerId())
                .staffId(order.getStaffId())
                .totalDue(order.getTotalDue())
                .priceShip(order.getPriceShip())
                .orderStatus(order.getOrderStatus())
                .typeOrder(order.getTypeOrder())
                .createdAt(order.getCreateAt())
                .items(items)
                .build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutOnline(
            @RequestBody CheckoutRequest request) {

        UUID orderId = orderService.checkoutOnline(request);

        return ResponseEntity.ok(orderId);
    }


}
