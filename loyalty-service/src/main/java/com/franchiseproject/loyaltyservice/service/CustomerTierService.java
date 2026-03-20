package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.response.CustomerTierResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;

import java.util.List;
import java.util.UUID;

public interface CustomerTierService {
    CustomerTierResponse getCustomerTierInfo(UUID customerId, UUID franchiseId);

    List<CustomerLoyaltyResponse> getCustomersByTier(CustomerLoyaltyTier tier);
}