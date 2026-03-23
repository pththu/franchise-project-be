package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.dto.request.CreateOrderRequest;
import com.franchiseproject.orderservice.dto.OrderResponse;
import com.franchiseproject.orderservice.dto.request.PaymentResultRequest;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
import com.franchiseproject.orderservice.dto.response.PaymentQRResponse;
import com.franchiseproject.orderservice.dto.response.PaymentResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
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


    /// createOrder cho staff tại các POS
    @PostMapping("/create-order")
    public ApiResponse<PaymentQRResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request
    ) {
        PaymentQRResponse paymentQRResponse = orderService.createOrder(request);
        return ApiResponse.<PaymentQRResponse>builder()
                .message("Tạo thành công!")
                .data(paymentQRResponse)
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

    @GetMapping("/customer/{customerId}")
    public ApiResponse<Page<OrderResponse>> getOrdersByCustomer(
            @PathVariable UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .message("Lấy danh sách đơn hàng theo mã khách hàng và trạng thái thành công")
                .statusCode(200)
                .data(orderService.getOrdersByCustomerIdAndStatus(customerId, status, page, size))
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


//    @PutMapping("/{orderId}")
//    public ApiResponse<OrderResponse> updateOrder(
//            @PathVariable UUID orderId,
//            @RequestBody @Valid UpdateOrderRequest request
//    ) {
//        OrderResponse response = orderService.updateOrder(orderId, request);
//
//        return ApiResponse.<OrderResponse>builder()
//                .message("Cập nhật đơn hàng thành công")
//                .data(response)
//                .statusCode(200)
//                .errors(null)
//                .build();
//    }

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

    @PostMapping("/payment-result")
    public ResponseEntity<String> receivePaymentResult(@RequestBody PaymentResultRequest request) {
        orderService.handlePaymentResult(request);
        return ResponseEntity.ok("Payment result received" + request.toString());
    }


    @GetMapping("/franchise/{franchiseId}")
    public ApiResponse<Page<OrderResponse>> getOrdersByFranchise(
            @PathVariable UUID franchiseId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .message("Lấy danh sách đơn hàng theo franchise thành công")
                .data(orderService.getOrdersByFranchiseAndStatus(franchiseId, status, page, size))
                .statusCode(200)
                .errors(null)
                .build();
    }

    @GetMapping("/status")
    public ApiResponse<Page<OrderResponse>> getOrdersByStatus(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<OrderResponse> orders = orderService.getOrdersByStatus(status, page, size);

        return ApiResponse.<Page<OrderResponse>>builder()
                .message("Lấy danh sách đơn hàng thành công")
                .data(orders)
                .statusCode(200)
                .errors(null)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<OrderResponse>> searchOrders(
            @RequestParam(required = false) UUID franchiseId,
            @RequestParam String keyword
    ) {

        List<OrderResponse> orders;

        if (franchiseId != null) {
            orders = orderService.searchOrders(franchiseId, keyword);
        } else {
            orders = orderService.searchOrderById(keyword);
        }

        return ApiResponse.<List<OrderResponse>>builder()
                .message("Search orders success")
                .data(orders)
                .statusCode(200)
                .errors(null)
                .build();
    }

    @GetMapping("/detail/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        return ApiResponse.<OrderResponse>builder()
                .message("Lấy đơn hàng theo mã đơn hàng thành công")
                .data(orderService.getOrderById(orderId))
                .build();
    }
}
