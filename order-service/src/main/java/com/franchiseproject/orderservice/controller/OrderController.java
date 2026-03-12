package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.dto.request.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.request.PaymentResultRequest;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.mapper.OrderMapper;
import com.franchiseproject.orderservice.service.OrderDetailService;
import com.franchiseproject.orderservice.service.OrderService;
import com.franchiseproject.orderservice.service.OrderStatusLogService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

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
    @PostMapping("/create-order")
    public ApiResponse<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(request);
        return ApiResponse.<OrderResponse>builder()
                .message("Tạo thành công!")
                .data(response)
                .statusCode(200)
                .errors(null)
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestParam UUID customerId
    ) {
        orderService.cancelOrder(orderId, customerId);
        return ApiResponse.<OrderResponse>builder()
                .message("Order has been cancelled")
                .statusCode(200)
                .errors(null)
                .build();
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
        OrderResponse response = orderService.updateOrder(orderId, request);

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

    /// Trả Response data của Order cho Payment-Service tạo Transaction
    /// dùng getOrder thì comment Mock test
//    @GetMapping("/{orderId}/get-orders")
//    public ApiResponse<PaymentResponse> getOrder(@PathVariable UUID orderId) {
//        return ApiResponse.<PaymentResponse>builder()
//                .message("Lấy đơn hàng trả về cho Payment-Service Thành Công!")
//                .data(orderService.getOrder(orderId))
//                .statusCode(200)
//                .errors(null)
//                .build();
//    }

    /// Mock Test Api với Payment-Service
    @GetMapping("/{orderId}/get-orders")
    public PaymentResponse getOrder(@PathVariable UUID orderId) {

        return PaymentResponse.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID())
                .finalTotal(new BigDecimal("120000"))
                .orderStatus(OrderStatus.WAITING_PAYMENT)
                .build();
    }
    @PostMapping("/payment-result")
    public ResponseEntity<String> receivePaymentResult(@RequestBody PaymentResultRequest request) {

        System.out.println("Received payment result: " + request);

        return ResponseEntity.ok("Payment result received");
    }


}
