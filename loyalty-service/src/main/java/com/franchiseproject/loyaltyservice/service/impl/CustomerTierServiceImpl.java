package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.CustomerTierResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import com.franchiseproject.loyaltyservice.repository.CustomerFranchiseRepository;
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

    CustomerFranchiseRepository customerFranchiseRepository;

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

    @Override
    public List<CustomerLoyaltyResponse> getCustomersByTier(CustomerLoyaltyTier tier) {
        List<CustomerFranchise> customerFranchises;

        if (tier == null) {
            customerFranchises = customerFranchiseRepository.findAll();
        } else {
            customerFranchises = customerFranchiseRepository.findByCustomerLoyaltyTier(tier);
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