package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.request.DeductPointsRequest;
import com.franchiseproject.loyaltyservice.dto.request.EarnPointsRequest;
import com.franchiseproject.loyaltyservice.dto.request.RefundPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.EarnPointsResponse;
import com.franchiseproject.loyaltyservice.dto.response.TransactionHistoryResponse;
import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTransactionType;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.mapper.LoyaltyMapper;
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.model.LoyaltyTier;
import com.franchiseproject.loyaltyservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTransactionRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTierRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoyaltyTransactionServiceImpl implements LoyaltyTransactionService {

    LoyaltyTransactionRepository loyaltyTransactionRepository;
    CustomerFranchiseRepository customerFranchiseRepository;
    LoyaltyMapper loyaltyMapper;
    LoyaltyTierRepository tierBenefitRepository;

    private static final double AMOUNT_PER_POINT = 10000.0;

    @Override
    public List<TransactionHistoryResponse> getByCustomerId(UUID customerId) {
        List<LoyaltyTransaction> transactions = loyaltyTransactionRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);

        return transactions.stream()
                .map(loyaltyMapper::toTransactionHistoryResponse)
                .toList();
    }

    /*** MANAGE POINTS ***/

    @Override
    @Transactional
    public EarnPointsResponse deductPoints(DeductPointsRequest request) {
        CustomerFranchise cf = customerFranchiseRepository
                .findByCustomerIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        int currentPoints = cf.getLoyaltyCurrentPoint();

        if (currentPoints < request.getPointsToDeduct()) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        int balanceBefore = currentPoints;
        int balanceAfter = balanceBefore - request.getPointsToDeduct();
        cf.setLoyaltyCurrentPoint(balanceAfter);
        customerFranchiseRepository.save(cf);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .customerId(request.getCustomerId())
                .franchiseId(request.getFranchiseId())
                .promotionId(UUID.fromString(request.getOrderId()))
                .points(-request.getPointsToDeduct())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(LoyaltyTransactionType.REDEEM)
                .createdAt(Instant.now())
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toEarnPointsResponse(transaction, cf.getCustomerLoyaltyTier().name());
    }

    @Override
    @Transactional
    public EarnPointsResponse earnPoints(EarnPointsRequest request) {

        int pointsEarned = (int) (request.getOrderAmount() / AMOUNT_PER_POINT);

        if (pointsEarned <= 0) {
            throw new AppException(ErrorCode.ORDER_AMOUNT_TOO_SMALL);
        }

        CustomerFranchise cf = customerFranchiseRepository
                .findByCustomerIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        int balanceBefore = cf.getLoyaltyCurrentPoint();
        int balanceAfter = balanceBefore + pointsEarned;
        int totalPointsAfter = cf.getLoyaltyTotalPoint() + pointsEarned;

        cf.setLoyaltyCurrentPoint(balanceAfter);
        cf.setLoyaltyTotalPoint(totalPointsAfter);

        CustomerLoyaltyTier newTier = determineTier(totalPointsAfter);
        cf.setCustomerLoyaltyTier(newTier);

        customerFranchiseRepository.save(cf);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .customerId(request.getCustomerId())
                .franchiseId(request.getFranchiseId())
                .points(pointsEarned)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(LoyaltyTransactionType.EARN)
                .createdAt(Instant.now())
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toEarnPointsResponse(transaction, newTier.name());
    }

    private CustomerLoyaltyTier determineTier(int totalPoints) {
        int diamondPts = getRequiredPoints("DIAMOND", 3000);
        int platinumPts = getRequiredPoints("PLATINUM", 2000);
        int goldPts = getRequiredPoints("GOLD", 1000);
        int silverPts = getRequiredPoints("SILVER", 500);

        if (totalPoints >= diamondPts) return CustomerLoyaltyTier.DIAMOND;
        if (totalPoints >= platinumPts) return CustomerLoyaltyTier.PLATINUM;
        if (totalPoints >= goldPts) return CustomerLoyaltyTier.GOLD;
        if (totalPoints >= silverPts) return CustomerLoyaltyTier.SILVER;

        return CustomerLoyaltyTier.BRONZE;
    }

    private int getRequiredPoints(String tierName, int defaultPoints) {
        return tierBenefitRepository.findById(tierName)
                .map(LoyaltyTier::getRequiredPoints)
                .orElse(defaultPoints);
    }

    @Override
    @Transactional
    public EarnPointsResponse refundPoints(RefundPointsRequest request) {
        boolean alreadyRefunded = loyaltyTransactionRepository.existsByCustomerIdAndPromotionIdAndType(
                request.getCustomerId(),
                UUID.fromString(request.getOrderId()),
                LoyaltyTransactionType.REFUND
        );

        if (alreadyRefunded) {
                throw new AppException(ErrorCode.ORDER_ALREADY_REFUNDED);
        }

        CustomerFranchise cf = customerFranchiseRepository
                .findByCustomerIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        int balanceBefore = cf.getLoyaltyCurrentPoint();
        int balanceAfter = balanceBefore + request.getPointsToRefund();
        cf.setLoyaltyCurrentPoint(balanceAfter);

        customerFranchiseRepository.save(cf);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .customerId(request.getCustomerId())
                .franchiseId(request.getFranchiseId())
                .promotionId(UUID.fromString(request.getOrderId()))
                .points(request.getPointsToRefund())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(LoyaltyTransactionType.REFUND)
                .createdAt(Instant.now())
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toEarnPointsResponse(transaction, cf.getCustomerLoyaltyTier().name());
    }
}