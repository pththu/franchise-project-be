package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.request.ManageTierBenefitRequest;
import com.franchiseproject.loyaltyservice.dto.response.TierBenefitResponse;
import java.util.List;

public interface TierBenefitService {
    List<TierBenefitResponse> getAllTierBenefits();
    void manageTierBenefits(ManageTierBenefitRequest request);
    void deleteTierBenefit(String tierName);
}