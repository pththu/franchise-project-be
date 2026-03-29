package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.LoyaltyWalletResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.model.LoyaltyWallet;
import com.franchiseproject.loyaltyservice.repository.LoyaltyWalletRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyWalletServiceImpl implements LoyaltyWalletService {

    private final LoyaltyWalletRepository walletRepository;

    @Override
    public LoyaltyWalletResponse getTierInfoFromWallet(UUID userId, UUID franchiseId) {
        LoyaltyWallet wallet = walletRepository.findByUserIdAndFranchiseId(userId, franchiseId)
                .orElse(null);

        if (wallet == null) {
            return LoyaltyWalletResponse.builder()
                    .userId(userId)
                    .franchiseId(franchiseId)
                    .currentTier(CustomerLoyaltyTier.BRONZE.name())
                    .currentPoints(0)
                    .totalPoints(0)
                    .build();
        }

        CustomerLoyaltyTier tier = wallet.getCustomerLoyaltyTier() != null ?
                wallet.getCustomerLoyaltyTier() : CustomerLoyaltyTier.BRONZE;

        return LoyaltyWalletResponse.builder()
                .userId(userId)
                .franchiseId(franchiseId)
                .currentTier(tier.name())
                .currentPoints(wallet.getLoyaltyCurrentPoint())
                .totalPoints(wallet.getLoyaltyTotalPoint())
                .build();
    }
}