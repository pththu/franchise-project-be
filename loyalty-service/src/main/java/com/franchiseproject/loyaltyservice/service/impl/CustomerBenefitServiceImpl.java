package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.CustomerBenefitResponse;
import com.franchiseproject.loyaltyservice.dto.response.CustomerLoyaltyResponse;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTier;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import com.franchiseproject.loyaltyservice.model.TierBenefit;
import com.franchiseproject.loyaltyservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.loyaltyservice.repository.TierBenefitRepository;
import com.franchiseproject.loyaltyservice.service.CustomerBenefitService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerBenefitServiceImpl implements CustomerBenefitService {

    CustomerFranchiseRepository customerFranchiseRepository;
    TierBenefitRepository tierBenefitRepository;

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
        return tierBenefitRepository.findById(tier.name())
                .map(TierBenefit::getBenefits)
                .orElse(new ArrayList<>());
    }

    @Override
    public List<CustomerLoyaltyResponse> getCustomersByTier(LoyaltyTier tier) {
        List<CustomerFranchise> customerFranchises;

        if (tier == null) {
            customerFranchises = customerFranchiseRepository.findAll();
        } else {
            customerFranchises = customerFranchiseRepository.findByLoyaltyTier(tier);
        }

        if (customerFranchises == null || customerFranchises.isEmpty()) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        return customerFranchises.stream()
                .map(cf -> CustomerLoyaltyResponse.builder()
                        .customerId(cf.getCustomerId())
                        .franchiseId(cf.getFranchiseId())
                        .loyaltyTier(cf.getLoyaltyTier())
                        .loyaltyCurrentPoint(cf.getLoyaltyCurrentPoint())
                        .loyaltyTotalPoint(cf.getLoyaltyTotalPoint())
                        .build())
                .toList();
    }
}