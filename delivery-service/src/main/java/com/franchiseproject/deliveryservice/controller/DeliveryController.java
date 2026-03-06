package com.franchiseproject.deliveryservice.controller;

import com.franchiseproject.deliveryservice.dto.ApiResponse;
import com.franchiseproject.deliveryservice.dto.request.CreateDeliveryRequest;
import com.franchiseproject.deliveryservice.dto.response.DeliveryResponse;
import com.franchiseproject.deliveryservice.model.Delivery;
import com.franchiseproject.deliveryservice.service.DeliveryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
}

