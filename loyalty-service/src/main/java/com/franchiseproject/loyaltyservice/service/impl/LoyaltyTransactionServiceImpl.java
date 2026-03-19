package com.franchiseproject.loyaltyservice.service.impl;

import com.franchiseproject.loyaltyservice.dto.request.EarnPointsRequest;
import com.franchiseproject.loyaltyservice.dto.request.RedeemPromotionRequest;
import com.franchiseproject.loyaltyservice.dto.response.EarnPointsResponse;
import com.franchiseproject.loyaltyservice.dto.response.PromotionDTO;
import com.franchiseproject.loyaltyservice.dto.response.RedeemPromotionResponse;
import com.franchiseproject.loyaltyservice.dto.response.TransactionHistoryResponse;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTier;
import com.franchiseproject.loyaltyservice.enums.LoyaltyTransactionType;
import com.franchiseproject.loyaltyservice.exception.AppException;
import com.franchiseproject.loyaltyservice.exception.ErrorCode;
import com.franchiseproject.loyaltyservice.mapper.LoyaltyMapper;
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.model.TierBenefit;
import com.franchiseproject.loyaltyservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTransactionRepository;
import com.franchiseproject.loyaltyservice.repository.TierBenefitRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoyaltyTransactionServiceImpl implements LoyaltyTransactionService {

    LoyaltyTransactionRepository loyaltyTransactionRepository;
    CustomerFranchiseRepository customerFranchiseRepository;
    RestTemplate restTemplate;
    LoyaltyMapper loyaltyMapper;
    TierBenefitRepository tierBenefitRepository;

    private static final double AMOUNT_PER_POINT = 10000.0;

    @Override
    public List<TransactionHistoryResponse> getByCustomerId(UUID customerId) {
        List<LoyaltyTransaction> transactions = loyaltyTransactionRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);

        return transactions.stream()
                .map(loyaltyMapper::toTransactionHistoryResponse)
                .toList();
    }

    /*** REDEEM PROMOTION ***/
    @Override
    @Transactional
    public RedeemPromotionResponse redeemPromotion(RedeemPromotionRequest request) {

        String promotionUrl = "http://localhost:3008/api/promotions/" + request.getPromotionId();
        PromotionDTO promotion;
        try {
            promotion = restTemplate.getForObject(promotionUrl, PromotionDTO.class);
        } catch (Exception e) {
            throw new AppException(ErrorCode.PROMOTION_NOT_FOUND);
        }

        if (promotion == null || !"ACTIVE".equalsIgnoreCase(promotion.getStatus())) {
            throw new AppException(ErrorCode.PROMOTION_NOT_FOUND);
        }

        Instant now = Instant.now();
        if ((promotion.getStartTime() != null && now.isBefore(promotion.getStartTime())) ||
                (promotion.getEndTime() != null && now.isAfter(promotion.getEndTime()))) {
            throw new AppException(ErrorCode.PROMOTION_EXPIRED);
        }

        int pointsRequired = promotion.getRequiredPoints() != null ? promotion.getRequiredPoints() : 0;
        if (pointsRequired <= 0) {
            throw new AppException(ErrorCode.INVALID_POINTS_AMOUNT);
        }

        CustomerFranchise cf = customerFranchiseRepository
                .findByCustomerIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        int currentPoints = cf.getLoyaltyCurrentPoint();

        if (currentPoints < pointsRequired) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        int balanceBefore = currentPoints;
        int balanceAfter = balanceBefore - pointsRequired;
        cf.setLoyaltyCurrentPoint(balanceAfter);
        customerFranchiseRepository.save(cf);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .customerId(request.getCustomerId())
                .franchiseId(cf.getFranchiseId())
                .promotionId(request.getPromotionId())
                .points(-pointsRequired)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(LoyaltyTransactionType.REDEEM)
                .createdAt(Instant.now())
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toRedeemPromotionResponse(transaction);
    }

    /*** EARN POINTS ***/

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

        LoyaltyTier newTier = determineTier(totalPointsAfter);
        cf.setLoyaltyTier(newTier);

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

    private LoyaltyTier determineTier(int totalPoints) {
        int diamondPts = getRequiredPoints("DIAMOND", 3000);
        int platinumPts = getRequiredPoints("PLATINUM", 2000);
        int goldPts = getRequiredPoints("GOLD", 1000);
        int silverPts = getRequiredPoints("SILVER", 500);

        if (totalPoints >= diamondPts) return LoyaltyTier.DIAMOND;
        if (totalPoints >= platinumPts) return LoyaltyTier.PLATINUM;
        if (totalPoints >= goldPts) return LoyaltyTier.GOLD;
        if (totalPoints >= silverPts) return LoyaltyTier.SILVER;

        return LoyaltyTier.BRONZE;
    }

    private int getRequiredPoints(String tierName, int defaultPoints) {
        return tierBenefitRepository.findById(tierName)
                .map(TierBenefit::getRequiredPoints)
                .orElse(defaultPoints);
    }
}