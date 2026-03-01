package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.config.LoyaltyProperties;
import com.franchiseproject.loyaltyservice.dto.response.TierBenefitResponse;
import com.franchiseproject.loyaltyservice.service.TierBenefitService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TierBenefitServiceImpl implements TierBenefitService {

    LoyaltyProperties loyaltyProperties;

    @Override
    public List<TierBenefitResponse> getAllTierBenefits() {
        List<TierBenefitResponse> responseList = new ArrayList<>();

        responseList.add(new TierBenefitResponse("BRONZE", 0,
                loyaltyProperties.getBenefits().getBronze()));

        responseList.add(new TierBenefitResponse("SILVER", loyaltyProperties.getTiers().getSilver(),
                loyaltyProperties.getBenefits().getSilver()));

        responseList.add(new TierBenefitResponse("GOLD", loyaltyProperties.getTiers().getGold(),
                loyaltyProperties.getBenefits().getGold()));

        responseList.add(new TierBenefitResponse("PLATINUM", loyaltyProperties.getTiers().getPlatinum(),
                loyaltyProperties.getBenefits().getPlatinum()));

        responseList.add(new TierBenefitResponse("DIAMOND", loyaltyProperties.getTiers().getDiamond(),
                loyaltyProperties.getBenefits().getDiamond()));

        return responseList;
    }
}