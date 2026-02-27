package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.dto.request.AddAddressRequest;
import com.franchiseproject.orderservice.dto.request.UpdateOrderRequest;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.dto.response.OrderDetailResponse;
import com.franchiseproject.orderservice.dto.response.OrderResponse;
import com.franchiseproject.orderservice.enums.OrderStatus;
import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.service.OrderDetailService;
import com.franchiseproject.orderservice.service.OrderService;
import com.franchiseproject.orderservice.service.OrderStatusLogService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {
    OrderService orderService;
    OrderDetailService orderDetailService;
    OrderStatusLogService orderStatusLogService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrder() {
        Map<String, Object> response = new HashMap<>();

        List<Order> orders = orderService.getAll();
        response.put("message", "Get All Orders");
        response.put("data", orders);

        return ResponseEntity.ok(response);
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

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @RequestBody @Valid UpdateOrderRequest request
    ) {
        Order order = orderService.updateOrder(request);
        OrderResponse response = mapToResponse(order);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .message("Cập nhật đơn hàng thành công")
                        .data(response)
                        .statusCode(200)
                        .errors(null)
                        .build()
        );
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderDetailResponse> orderDetails = order.getOrderDetails()
                .stream()
                .map(detail -> OrderDetailResponse.builder()
                        .productId(detail.getProductId())
                        .productNameSnapshot(detail.getProductNameSnapshot())
                        .priceSnapshot(detail.getPriceSnapshot())
                        .quantity(detail.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .franchiseId(order.getFranchiseId())
                .customerId(order.getCustomerId())
                .staffId(order.getStaffId())
                .totalDue(order.getTotalDue())
                .priceShip(order.getPriceShip())
                .orderStatus(order.getOrderStatus())
                .typeOrder(order.getTypeOrder())
                .createAt(order.getCreateAt())
                .orderDetails(orderDetails)
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
