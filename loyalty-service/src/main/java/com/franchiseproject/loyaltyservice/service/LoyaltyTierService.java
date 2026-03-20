package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.request.ManageTierRequest;
import com.franchiseproject.loyaltyservice.dto.response.LoyaltyTierResponse;
import java.util.List;

public interface LoyaltyTierService {
    List<LoyaltyTierResponse> getAllTiers();
    void manageTier(ManageTierRequest request);
    void deleteTier(String tierName);
}