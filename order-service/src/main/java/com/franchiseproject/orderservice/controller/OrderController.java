package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.dto.CheckoutRequest;
import com.franchiseproject.orderservice.dto.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.mapper.OrderMapper;
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
import java.time.Instant;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {
    OrderService orderService;
    OrderDetailService orderDetailService;
    OrderStatusLogService orderStatusLogService;
    OrderMapper orderMapper;

    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrder() {

        List<OrderResponse> orders = orderService.getAll();

        return ApiResponse.<List<OrderResponse>>builder()
                .message("Lấy danh sách đơn hàng thành công")
                .data(orders)
                .statusCode(200)
                .errors(null)
                .build();
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


    @PutMapping("/{orderId}")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid UpdateOrderRequest request
    ) {
        Order order = orderService.updateOrder(orderId, request);
        OrderResponse response = orderMapper.toOrderResponse(order);

        return ApiResponse.<OrderResponse>builder()
                        .message("Cập nhật đơn hàng thành công")
                        .data(response)
                        .statusCode(200)
                        .errors(null)
                        .build();
    }

    // Thêm địa chỉ cho đơn hàng Online
    @PostMapping("/online/address")
    public ApiResponse<Void> addAddressOnlineOrder(
            @Valid @RequestBody AddAddressRequest request) {
        orderService.addAddressOnlineOrder(request);
        return ApiResponse.<Void>builder()
                .message("Thêm địa chỉ cho đơn hàng Online thành công")
                .data(null)
                .statusCode(200)
                .errors(null)
                .build();
    }

    // Lấy địa chỉ của đơn hàng Online
    @GetMapping("/online/{customerId}/address")
    public ApiResponse<String> getAddressOnlineOrder(
            @PathVariable UUID customerId) {
        return ApiResponse.<String>builder()
                .message("Lấy địa chỉ đơn hàng Online thành công")
                .data(orderService.getAddressOnlineOrder(customerId))
                .statusCode(200)
                .errors(null)
                .build();
    }
}
