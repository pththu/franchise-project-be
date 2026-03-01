package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.request.ManualAdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.ManualAdjustPointsResponse;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTransactionType;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTransactionRepository;
import com.franchiseproject.loyaltyservice.service.PointAdjustmentService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PointAdjustmentServiceImpl implements PointAdjustmentService {

    CustomerFranchiseRepository customerFranchiseRepository;
    LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Override
    @Transactional
    public ManualAdjustPointsResponse manuallyAdjustPoints(ManualAdjustPointsRequest request) {
        if (request.getPoints() == 0) {
            throw new AppException(ErrorCode.POINTS_CANNOT_BE_ZERO);
        }

        CustomerFranchise cf = customerFranchiseRepository
                .findByCustomerIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        int balanceBefore = cf.getLoyaltyCurrentPoint();
        int newBalance = balanceBefore + request.getPoints();

        if (newBalance < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        cf.setLoyaltyCurrentPoint(newBalance);
        if (request.getPoints() > 0) {
            cf.setLoyaltyTotalPoint(cf.getLoyaltyTotalPoint() + request.getPoints());
        }
        customerFranchiseRepository.save(cf);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .customerId(request.getCustomerId())
                .franchiseId(request.getFranchiseId())
                .points(request.getPoints())
                .balanceBefore(balanceBefore)
                .balanceAfter(newBalance)
                .type(LoyaltyTransactionType.MANUAL)
                .description(request.getReason())
                .createdAt(Instant.now())
                .build();
        loyaltyTransactionRepository.save(transaction);

        return ManualAdjustPointsResponse.builder()
                .transactionId(transaction.getId())
                .customerId(cf.getCustomerId())
                .pointsAdjusted(request.getPoints())
                .balanceAfter(newBalance)
                .adjustmentReason(request.getReason())
                .timestamp(transaction.getCreatedAt())
                .build();
    }
}