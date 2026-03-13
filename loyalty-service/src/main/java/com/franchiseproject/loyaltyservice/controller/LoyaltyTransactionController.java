package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.request.EarnPointsRequest;
import com.franchiseproject.loyaltyservice.dto.request.RedeemPromotionRequest;
import com.franchiseproject.loyaltyservice.dto.response.EarnPointsResponse;
import com.franchiseproject.loyaltyservice.dto.response.RedeemPromotionResponse;
import com.franchiseproject.loyaltyservice.dto.response.TransactionHistoryResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyTransactionController {

    LoyaltyTransactionService loyaltyTransactionService;

    @GetMapping("/transactions/{customerId}")
    public ApiResponse<List<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable UUID customerId) {

        return ApiResponse.<List<TransactionHistoryResponse>>builder()
                .statusCode(200)
                .message("Get transaction history successfully")
                .data(loyaltyTransactionService.getByCustomerId(customerId))
                .build();
    }

    @PostMapping("/redeem")
    public ApiResponse<RedeemPromotionResponse> redeemPromotion(@Valid @RequestBody RedeemPromotionRequest request) {
        return ApiResponse.<RedeemPromotionResponse>builder()
                .statusCode(200)
                .message("Redeem promotion successfully")
                .data(loyaltyTransactionService.redeemPromotion(request))
                .build();
    }

    @PostMapping("/earn")
    public ApiResponse<EarnPointsResponse> earnPoints(@Valid @RequestBody EarnPointsRequest request) {
        return ApiResponse.<EarnPointsResponse>builder()
                .statusCode(200)
                .message("Earn points successfully")
                .data(loyaltyTransactionService.earnPoints(request))
                .build();
    }
}