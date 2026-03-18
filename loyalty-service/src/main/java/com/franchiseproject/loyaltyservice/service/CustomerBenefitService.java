package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.response.CustomerBenefitResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTier;

import java.util.List;
import java.util.UUID;

public interface CustomerBenefitService {
    CustomerBenefitResponse getCustomerBenefits(UUID customerId, UUID franchiseId);

    List<CustomerLoyaltyResponse> getCustomersByTier(LoyaltyTier tier);
}