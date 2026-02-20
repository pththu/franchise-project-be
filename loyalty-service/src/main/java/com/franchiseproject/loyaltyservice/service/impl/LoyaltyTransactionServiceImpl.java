package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.request.AdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.AdjustPointsResponse;
import com.franchiseproject.loyaltyservice.enums.LoyalyTransactionType;
import com.franchiseproject.loyaltyservice.exception.ResourceNotFoundException;
import com.franchiseproject.loyaltyservice.mapper.LoyaltyMapper;
import com.franchiseproject.loyaltyservice.model.CustomerLoyalty;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.repository.CustomerLoyaltyRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTransactionRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoyaltyTransactionServiceImpl implements LoyaltyTransactionService {

    LoyaltyTransactionRepository loyaltyTransactionRepository;
    CustomerLoyaltyRepository customerLoyaltyRepository;
    LoyaltyMapper loyaltyMapper;

    @Override
    public List<LoyaltyTransaction> getByCustomerId(UUID customerId) {
        return loyaltyTransactionRepository.findLoyaltyTransactionsByCustomerId(customerId);
    }

    @Override
    @Transactional
    public AdjustPointsResponse adjustPoints(AdjustPointsRequest request) {
        CustomerLoyalty loyalty = customerLoyaltyRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer loyalty profile not found"));

        int balanceBefore = loyalty.getCurrentPoints() != null ? loyalty.getCurrentPoints() : 0;
        int pointsToAdjust = request.getPoints();
        int newBalance = balanceBefore + pointsToAdjust;

        if (newBalance < 0) {
            throw new IllegalArgumentException("Cannot deduct more points than the current balance. Current balance: " + balanceBefore);
        }

        loyalty.setCurrentPoints(newBalance);

        if (pointsToAdjust > 0) {
            int totalPoints = loyalty.getTotalPoints() != null ? loyalty.getTotalPoints() : 0;
            loyalty.setTotalPoints(totalPoints + pointsToAdjust);
        }
        customerLoyaltyRepository.save(loyalty);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .customerId(request.getCustomerId())
                .balanceBefore(balanceBefore)
                .balanceAfter(newBalance)
                .points(pointsToAdjust)
                .type(LoyalyTransactionType.MANUAL)
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toAdjustResponse(transaction);
    }
}