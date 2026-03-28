package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.CustomerTierResponse;
import com.franchiseproject.loyaltyservice.dto.response.LoyaltyWalletResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
<<<<<<< HEAD
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import com.franchiseproject.loyaltyservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.loyaltyservice.repository.CustomerRepository;
import com.franchiseproject.loyaltyservice.model.Customer;
=======
import com.franchiseproject.loyaltyservice.model.LoyaltyWallet;
import com.franchiseproject.loyaltyservice.repository.LoyaltyWalletRepository;
>>>>>>> sprint04
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

<<<<<<< HEAD
    CustomerFranchiseRepository customerFranchiseRepository;
    CustomerRepository customerRepository;

    @Override
    public CustomerTierResponse getCustomerTierInfoByPhone(String phone, UUID franchiseId) {
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        
        CustomerTierResponse response = getCustomerTierInfo(customer.getId(), franchiseId);
        response.setCustomerName(customer.getFullName());
        response.setCustomerPhone(customer.getPhone());
        return response;
    }

    @Override
    public CustomerTierResponse getCustomerTierInfo(UUID customerId, UUID franchiseId) {
        CustomerFranchise cf = customerFranchiseRepository
                .findByCustomerIdAndFranchiseId(customerId, franchiseId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        CustomerLoyaltyTier currentTier = cf.getCustomerLoyaltyTier();
        if (currentTier == null) {
            currentTier = CustomerLoyaltyTier.BRONZE;
        }

        return CustomerTierResponse.builder()
                .customerId(customerId)
                .franchiseId(franchiseId)
                .currentTier(currentTier.name())
                .currentPoints(cf.getLoyaltyCurrentPoint())
                .totalPoints(cf.getLoyaltyTotalPoint())
                .build();
    }
=======
    LoyaltyWalletRepository loyaltyWalletRepository;
>>>>>>> sprint04

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

    @Override
    public List<CustomerTierResponse> getBulkCustomerTierInfo(List<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<LoyaltyWallet> wallets = loyaltyWalletRepository.findByUserIdIn(customerIds);

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