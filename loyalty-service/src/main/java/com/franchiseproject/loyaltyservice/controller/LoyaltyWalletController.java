package com.franchiseproject.loyaltyservice.controller;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import com.franchiseproject.loyaltyservice.dto.response.LoyaltyWalletResponse;
import com.franchiseproject.loyaltyservice.service.LoyaltyWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyWalletController {

    private final LoyaltyWalletService loyaltyWalletService;

    @GetMapping("/wallets/users/{userId}")
    public ApiResponse<LoyaltyWalletResponse> getUserWallet(
            @PathVariable UUID userId) {

        LoyaltyWalletResponse tierInfo = loyaltyWalletService.getTierInfoFromWallet(userId);

        return ApiResponse.<LoyaltyWalletResponse>builder()
                .statusCode(200)
                .message("Get user tier info successfully")
                .data(tierInfo)
                .build();
    }

    @PostMapping("/wallets/users/{userId}")
    public ApiResponse<LoyaltyWalletResponse> createWallet(@PathVariable UUID userId) {
        return ApiResponse.<LoyaltyWalletResponse>builder()
                .statusCode(201) // Mã 201 Created
                .message("Loyalty wallet created successfully")
                .data(loyaltyWalletService.createWallet(userId))
                .build();
    }
}