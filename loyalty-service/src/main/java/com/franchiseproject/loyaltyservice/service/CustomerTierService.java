package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;

import java.util.List;

public interface CustomerTierService {
    List<CustomerLoyaltyResponse> getCustomersByTier(CustomerLoyaltyTier tier);
}