package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.config.LoyaltyProperties;
import com.franchiseproject.loyaltyservice.dto.response.CustomerBenefitResponse;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTier;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import com.franchiseproject.loyaltyservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.loyaltyservice.service.CustomerBenefitService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerBenefitServiceImpl implements CustomerBenefitService {

    CustomerFranchiseRepository customerFranchiseRepository;
    LoyaltyProperties loyaltyProperties;

    @Override
    public CustomerBenefitResponse getCustomerBenefits(UUID customerId, UUID franchiseId) {
        CustomerFranchise cf = customerFranchiseRepository
                .findByCustomerIdAndFranchiseId(customerId, franchiseId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        LoyaltyTier currentTier = cf.getLoyaltyTier();
        if (currentTier == null) {
            currentTier = LoyaltyTier.BRONZE;
        }

        List<String> benefits = getBenefitsByTier(currentTier);

        return CustomerBenefitResponse.builder()
                .customerId(customerId)
                .franchiseId(franchiseId)
                .currentTier(currentTier.name())
                .currentPoints(cf.getLoyaltyCurrentPoint())
                .totalPoints(cf.getLoyaltyTotalPoint())
                .currentBenefits(benefits)
                .build();
    }

    private List<String> getBenefitsByTier(LoyaltyTier tier) {
        return switch (tier) {
            case SILVER -> loyaltyProperties.getBenefits().getSilver();
            case GOLD -> loyaltyProperties.getBenefits().getGold();
            case PLATINUM -> loyaltyProperties.getBenefits().getPlatinum();
            case DIAMOND -> loyaltyProperties.getBenefits().getDiamond();
            default -> loyaltyProperties.getBenefits().getBronze();
        };
    }
}