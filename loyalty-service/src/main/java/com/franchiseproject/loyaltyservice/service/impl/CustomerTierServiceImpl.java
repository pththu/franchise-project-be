package com.franchiseproject.loyaltyservice.service.impl;

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

import java.util.List;
import java.util.UUID;

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
                        .franchiseId(cf.getFranchiseId())
                        .customerLoyaltyTier(cf.getCustomerLoyaltyTier())
                        .loyaltyCurrentPoint(cf.getLoyaltyCurrentPoint())
                        .loyaltyTotalPoint(cf.getLoyaltyTotalPoint())
                        .build())
                .toList();
    }
}