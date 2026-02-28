package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.dto.CheckoutRequest;
import com.franchiseproject.orderservice.dto.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.mapper.OrderMapper;
import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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
    OrderMapper orderMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrder() {
        Map<String, Object> response = new HashMap<>();
        List<Order> orders = orderService.getAll();
        response.put("message", "Get All Orders");
        response.put("data", orderService.getAll());

        return ResponseEntity.ok(response);
    }


    //createOrder cho staff tại các POS
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request
    ) {
        Order order = orderService.createOrder(request);
        OrderResponse response = orderMapper.toOrderResponse(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutOnline(
            @RequestBody CheckoutRequest request) {

        UUID orderId = orderService.checkoutOnline(request);

        return ResponseEntity.ok(orderId);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable UUID orderId,
            @RequestParam UUID customerId
    ) {
        orderService.cancelOrder(orderId, customerId);
        return ResponseEntity.ok("Order cancelled successfully");
    }

    @PatchMapping("/{orderId}/status")
    public ApiResponse<Void> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(orderId, status);
        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái đơn hàng thành công")
                .statusCode(200)
                .data(null)
                .errors(null)
                .build();
    }

    @GetMapping("/{customerId}")
    public ApiResponse<List<OrderResponse>> getOrderByCustomerId(@PathVariable UUID customerId) {
        return ApiResponse.<List<OrderResponse>>builder()
                .message("Tìm đơn hàng theo mã khách hàng thành công!!!")
                .statusCode(200)
                .data(orderService.getOrderByCustomerId(customerId))
                .build();
    }

    @PutMapping("/{orderId}/assign-staff/{staffId}")
    public ResponseEntity<?> assignStaff(
            @PathVariable UUID orderId,
            @PathVariable UUID staffId
    ) {
        orderService.assignStaff(orderId, staffId);
        return ResponseEntity.ok("Assign staff success");
    }
    @PutMapping("/{orderId}/mark-special")
    public ResponseEntity<?> markSpecial(@PathVariable UUID orderId) {
        orderService.markSpecial(orderId);
        return ResponseEntity.ok("Mark special success");
    }
    @PutMapping("/{orderId}/estimate-delivery")
    public ResponseEntity<?> estimateDelivery(
            @PathVariable UUID orderId,
            @RequestParam("eta") String eta
    ) {
        Instant estimatedTime = Instant.parse(eta);
        orderService.estimateDeliveryTime(orderId, estimatedTime);
        return ResponseEntity.ok("Estimate delivery time success");
    }
}
