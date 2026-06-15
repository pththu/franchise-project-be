package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.response.LoyaltyReportResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTransactionType;
import com.franchiseproject.loyaltyservice.repository.LoyaltyWalletRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTransactionRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyReportService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoyaltyReportServiceImpl implements LoyaltyReportService {

    LoyaltyTransactionRepository loyaltyTransactionRepository;
    LoyaltyWalletRepository loyaltyWalletRepository;

    @Override
    public LoyaltyReportResponse getLoyaltyReport() {
        Long totalEarned = loyaltyTransactionRepository.sumPointsByType(LoyaltyTransactionType.EARN);
        Long totalRedeemed = loyaltyTransactionRepository.sumPointsByType(LoyaltyTransactionType.REDEEM);
        Long earnCount = loyaltyTransactionRepository.countTransactionsByType(LoyaltyTransactionType.EARN);
        Long redeemCount = loyaltyTransactionRepository.countTransactionsByType(LoyaltyTransactionType.REDEEM);

        List<Object[]> tierData = loyaltyWalletRepository.countCustomersByTier();
        Map<String, Long> customersByTier = new HashMap<>();

        for (Object[] row : tierData) {
            CustomerLoyaltyTier tier = (CustomerLoyaltyTier) row[0];
            Long count = (Long) row[1];
            String tierName = (tier != null) ? tier.name() : "UNRANKED";
            customersByTier.put(tierName, count);
        }

        return LoyaltyReportResponse.builder()
                .totalPointsEarned(totalEarned)
                .totalPointsRedeemed(totalRedeemed)
                .totalEarnTransactions(earnCount)
                .totalRedeemTransactions(redeemCount)
                .customerByTier(customersByTier)
                .build();
    }
}