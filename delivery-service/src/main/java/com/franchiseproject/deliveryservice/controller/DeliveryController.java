package com.franchiseproject.deliveryservice.controller;

import com.franchiseproject.deliveryservice.dto.ApiResponse;
import com.franchiseproject.deliveryservice.dto.request.CreateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.request.UpdateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.response.DeliveryResponse;
import com.franchiseproject.deliveryservice.model.Delivery;
import com.franchiseproject.deliveryservice.service.DeliveryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequestMapping("/api/delivery")
public class DeliveryController {
    DeliveryService deliveryService;

    @GetMapping("/getall")
    public List<Delivery> findAll() {
        return deliveryService.findAll();
    }

    @GetMapping("/get-delivery-by-order/{orderId}")
    public ApiResponse<DeliveryResponse> getDeliveryByOrderId(@PathVariable UUID orderId) {
        DeliveryResponse response = deliveryService.getDeliveryByOrderId(orderId);
        return ApiResponse.<DeliveryResponse>builder()
                .message("Lấy thông tin đơn giao hàng thành công")
                .data(response)
                .statusCode(200)
                .errors(null)
                .build();
    }

    @PostMapping("/create-delivery")
    public ApiResponse<DeliveryResponse> createDelivery(@RequestBody CreateDeliveryRequest request) {
        DeliveryResponse response = deliveryService.createDelivery(request);
        return ApiResponse.<DeliveryResponse>builder()
                .message("Tạo đơn giao hàng thành công")
                .data(response)
                .statusCode(200)
                .errors(null)
                .build();
    }

    @PutMapping("/assign-shipper/{deliveryId}")
    public ApiResponse<DeliveryResponse> assignShipper(
            @PathVariable UUID deliveryId,
            @RequestBody UpdateDeliveryRequest request) {
        DeliveryResponse response = deliveryService.updateDelivery(deliveryId, request);
        return ApiResponse.<DeliveryResponse>builder()
                .message("Gán nhân viên giao hàng thành công")
                .data(response)
                .statusCode(200)
                .errors(null)
                .build();
    }

    @PutMapping("/update-delivery/{deliveryId}")
    public ApiResponse<DeliveryResponse> updateDelivery(
            @PathVariable UUID deliveryId,
            @RequestBody UpdateDeliveryRequest request) {
        DeliveryResponse response = deliveryService.updateDelivery(deliveryId, request);
        return ApiResponse.<DeliveryResponse>builder()
                .message("Cập nhật đơn giao hàng thành công")
                .data(response)
                .statusCode(200)
                .errors(null)
                .build();
    }
}

