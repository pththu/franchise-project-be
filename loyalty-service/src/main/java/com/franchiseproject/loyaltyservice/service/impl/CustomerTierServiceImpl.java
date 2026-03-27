package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.CustomerTierResponse;
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
    public CustomerTierResponse getCustomerTierInfo(UUID customerId, UUID franchiseId) {
        LoyaltyWallet wallet = loyaltyWalletRepository
                .findByCustomerIdAndFranchiseId(customerId, franchiseId)
                .orElseThrow(() -> new AppException(ErrorCode.LOYALTY_WALLET_NOT_FOUND));

        CustomerLoyaltyTier currentTier = wallet.getCustomerLoyaltyTier();
        if (currentTier == null) {
            currentTier = CustomerLoyaltyTier.BRONZE;
        }

        return CustomerTierResponse.builder()
                .customerId(customerId)
                .franchiseId(franchiseId)
                .currentTier(currentTier.name())
                .currentPoints(wallet.getLoyaltyCurrentPoint())
                .totalPoints(wallet.getLoyaltyTotalPoint())
                .build();
    }

    @Override
    public List<CustomerLoyaltyResponse> getCustomersByTier(CustomerLoyaltyTier tier) {
        List<LoyaltyWallet> customerFranchises;

        if (tier == null) {
            customerFranchises = loyaltyWalletRepository.findAll();
        } else {
            customerFranchises = loyaltyWalletRepository.findByCustomerLoyaltyTier(tier);
        }

        return customerFranchises.stream()
                .map(cf -> CustomerLoyaltyResponse.builder()
                        .customerId(cf.getCustomerId())
                        .franchiseId(cf.getFranchiseId())
                        .customerLoyaltyTier(cf.getCustomerLoyaltyTier())
                        .loyaltyCurrentPoint(cf.getLoyaltyCurrentPoint())
                        .loyaltyTotalPoint(cf.getLoyaltyTotalPoint())
                        .build())
                .toList();
    }
}