package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.request.ManageTierRequest;
import com.franchiseproject.loyaltyservice.dto.response.LoyaltyTierResponse;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.model.LoyaltyTier;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTierRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyTierService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoyaltyTierServiceImpl implements LoyaltyTierService {

    LoyaltyTierRepository loyaltyTierRepository;

    @PostConstruct
    public void initDataToDatabase() {
        if (loyaltyTierRepository.count() == 0) {
            loyaltyTierRepository.save(new LoyaltyTier("BRONZE", 0));

            loyaltyTierRepository.save(new LoyaltyTier("SILVER", 500));

            loyaltyTierRepository.save(new LoyaltyTier("GOLD", 1000));

            loyaltyTierRepository.save(new LoyaltyTier("PLATINUM", 2000));

            loyaltyTierRepository.save(new LoyaltyTier("DIAMOND", 3000));
        }
    }

    @Override
    public List<LoyaltyTierResponse> getAllTiers() {
        return loyaltyTierRepository.findAll().stream()
                .map(tier -> new LoyaltyTierResponse(tier.getTierName(), tier.getRequiredPoints()))
                .toList();
    }

    @Override
    @Transactional
    public void manageTier(ManageTierRequest request) {
        String tierName = request.getTierName().toUpperCase().trim();

        LoyaltyTier loyaltyTier = loyaltyTierRepository.findById(tierName).orElse(null);

        if (loyaltyTier == null) {
            loyaltyTier = new LoyaltyTier(tierName, request.getRequiredPoints());

        } else {
            loyaltyTier.setRequiredPoints(request.getRequiredPoints());
        }

        loyaltyTierRepository.save(loyaltyTier);
    }

    @Override
    @Transactional
    public void deleteTier(String tierName) {
        String normalizedTierName = tierName.toUpperCase().trim();

        if (!loyaltyTierRepository.existsById(normalizedTierName)) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        loyaltyTierRepository.deleteById(normalizedTierName);
    }
}