package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.CustomerTierResponse;
import com.franchiseproject.loyaltyservice.dto.response.LoyaltyWalletResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.model.LoyaltyWallet;
import com.franchiseproject.loyaltyservice.repository.LoyaltyWalletRepository;
import com.franchiseproject.loyaltyservice.service.CustomerTierService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerTierServiceImpl implements CustomerTierService {

    LoyaltyWalletRepository loyaltyWalletRepository;

    @Override
    public List<CustomerLoyaltyResponse> getCustomersByTier(CustomerLoyaltyTier tier) {
        List<LoyaltyWallet> wallets;

        if (tier == null) {
            wallets = loyaltyWalletRepository.findAll();
        } else {
            wallets = loyaltyWalletRepository.findByCustomerLoyaltyTier(tier);
        }

        return wallets.stream()
                .map(cf -> CustomerLoyaltyResponse.builder()
                        .userId(cf.getUserId())
                        .customerLoyaltyTier(cf.getCustomerLoyaltyTier())
                        .loyaltyCurrentPoint(cf.getLoyaltyCurrentPoint())
                        .loyaltyTotalPoint(cf.getLoyaltyTotalPoint())
                        .build())
                .toList();
    }

    @Override
    public List<CustomerTierResponse> getBulkCustomerTierInfo(List<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<LoyaltyWallet> all = loyaltyWalletRepository.findAll();
        List<LoyaltyWallet> wallets = loyaltyWalletRepository.findByUserIdIn(customerIds);


        System.out.println("wallets size: " + wallets.size());
        System.out.println("wallets getUserId: " + wallets.get(0).getUserId());
        System.out.println("all size: " + all.size());
        System.out.println("all getUserId: " + all.get(0).getUserId());

        return wallets.stream().map(wallet ->
                CustomerTierResponse.builder()
                        .userId(wallet.getUserId())
                        .loyaltyTier(wallet.getCustomerLoyaltyTier())
                        .currentPoint(wallet.getLoyaltyCurrentPoint())
                        .totalPoint(wallet.getLoyaltyTotalPoint())
                        .build()
        ).collect(Collectors.toList());
    }
}