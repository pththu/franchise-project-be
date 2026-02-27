package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.model.Order;
import com.franchiseproject.orderservice.service.OrderDetailService;
import com.franchiseproject.orderservice.service.OrderService;
import com.franchiseproject.orderservice.service.OrderStatusLogService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")

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
        response.put("data", orderService.getAll());

        return ResponseEntity.ok(response);
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
