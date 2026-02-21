package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.request.AdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.AdjustPointsResponse;
import com.franchiseproject.loyaltyservice.enums.LoyalyTransactionType;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
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

import java.time.Instant;
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
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        int balanceBefore = loyalty.getCurrentPoints() != null ? loyalty.getCurrentPoints() : 0;
        int pointsToAdjust = request.getPoints();
        int newBalance = balanceBefore + pointsToAdjust;

        if (newBalance < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS_BALANCE);
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
                .createdAt(Instant.now()) // Thêm dòng này để DTO trả về không bị null timestamp
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toAdjustResponse(transaction);
    }
}