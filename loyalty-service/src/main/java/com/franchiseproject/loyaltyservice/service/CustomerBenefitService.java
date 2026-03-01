package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.response.CustomerBenefitResponse;

import java.util.UUID;

public interface CustomerBenefitService {
    CustomerBenefitResponse getCustomerBenefits(UUID customerId, UUID franchiseId);
}