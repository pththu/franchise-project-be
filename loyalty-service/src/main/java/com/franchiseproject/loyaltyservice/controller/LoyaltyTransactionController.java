package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.request.AdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.AdjustPointsResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
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
@RequestMapping("/api/v1/loyalty")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyTransactionController {

    LoyaltyTransactionService loyaltyTransactionService;

    @GetMapping("/transactions/{customerId}")
    public ApiResponse<List<LoyaltyTransaction>> getLoyaltyTransactionByCustomerId(@PathVariable("customerId") UUID customerId) {
        return ApiResponse.<List<LoyaltyTransaction>>builder()
                .statusCode(200)
                .message("Get All loyaltyTransactions")
                .data(loyaltyTransactionService.getByCustomerId(customerId))
                .build();
    }

    @PostMapping("/adjust")
    public ApiResponse<AdjustPointsResponse> adjustPoints(@RequestBody @Valid AdjustPointsRequest request) {
        return ApiResponse.<AdjustPointsResponse>builder()
                .statusCode(200)
                .message("Adjust points successful")
                .data(loyaltyTransactionService.adjustPoints(request))
                .build();
    }
}