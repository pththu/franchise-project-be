package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.LoyaltyWalletResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.mapper.LoyaltyMapper;
import com.franchiseproject.loyaltyservice.model.LoyaltyWallet;
import com.franchiseproject.loyaltyservice.repository.LoyaltyWalletRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyWalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyWalletServiceImpl implements LoyaltyWalletService {

    private final LoyaltyWalletRepository loyaltyWalletRepository;
    LoyaltyMapper loyaltyMapper;

    @Override
    public LoyaltyWalletResponse getTierInfoFromWallet(UUID userId) {
        LoyaltyWallet wallet = loyaltyWalletRepository.findByUserId(userId)
                .orElse(null);

        if (wallet == null) {
            return LoyaltyWalletResponse.builder()
                    .userId(userId)
                    .currentTier(CustomerLoyaltyTier.BRONZE.name())
                    .currentPoints(0)
                    .totalPoints(0)
                    .build();
        }

        CustomerLoyaltyTier tier = wallet.getCustomerLoyaltyTier() != null ?
                wallet.getCustomerLoyaltyTier() : CustomerLoyaltyTier.BRONZE;

        return LoyaltyWalletResponse.builder()
                .userId(userId)
                .currentTier(tier.name())
                .currentPoints(wallet.getLoyaltyCurrentPoint())
                .totalPoints(wallet.getLoyaltyTotalPoint())
                .build();
    }

    @Override
    @Transactional
    public LoyaltyWalletResponse createWallet(UUID userId) {
        boolean exists = loyaltyWalletRepository.findByUserId(userId).isPresent();
        if (exists) {
            throw new AppException(ErrorCode.LOYALTY_WALLET_ALREADY_EXISTS);
        }

        LoyaltyWallet newWallet = LoyaltyWallet.builder()
                .userId(userId)
                .loyaltyCurrentPoint(0)
                .loyaltyTotalPoint(0)
                .customerLoyaltyTier(CustomerLoyaltyTier.BRONZE)
                // .isActive(true) // Trường status/active
                .build();

        newWallet = loyaltyWalletRepository.save(newWallet);

        return loyaltyMapper.toLoyaltyWalletResponse(newWallet);
    }
}