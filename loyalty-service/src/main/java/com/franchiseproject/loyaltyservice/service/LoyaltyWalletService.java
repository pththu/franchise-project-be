package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.response.LoyaltyWalletResponse;

import java.util.UUID;

public interface LoyaltyWalletService {
    LoyaltyWalletResponse getTierInfoFromWallet(UUID userId);
}