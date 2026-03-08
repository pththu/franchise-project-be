package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.response.PromotionResponse;
import com.franchiseproject.loyaltyservice.service.PromotionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionController {

    PromotionService promotionService;

    @GetMapping("/promotions")
    public ApiResponse<List<PromotionResponse>> getAvailablePromotions() {
        return ApiResponse.<List<PromotionResponse>>builder()
                .statusCode(200)
                .message("Get available promotions successfully")
                .data(promotionService.getAvailablePromotions())
                .build();
    }
}