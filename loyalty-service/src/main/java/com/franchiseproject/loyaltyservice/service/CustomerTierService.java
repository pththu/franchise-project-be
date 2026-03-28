package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerTierResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;

import java.util.List;
import java.util.UUID;

public interface CustomerTierService {
<<<<<<< HEAD
    CustomerTierResponse getCustomerTierInfo(UUID customerId, UUID franchiseId);

    CustomerTierResponse getCustomerTierInfoByPhone(String phone, UUID franchiseId);

=======
>>>>>>> sprint04
    List<CustomerLoyaltyResponse> getCustomersByTier(CustomerLoyaltyTier tier);

    List<CustomerTierResponse> getBulkCustomerTierInfo(List<UUID> customerIds);
}