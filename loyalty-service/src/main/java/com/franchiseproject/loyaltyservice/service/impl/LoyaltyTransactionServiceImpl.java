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
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import com.franchiseproject.loyaltyservice.model.LoyaltyTier;
import com.franchiseproject.loyaltyservice.model.LoyaltyWallet;
import com.franchiseproject.loyaltyservice.repository.LoyaltyWalletRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTransactionRepository;
import com.franchiseproject.loyaltyservice.repository.LoyaltyTierRepository;
import com.franchiseproject.loyaltyservice.service.LoyaltyTransactionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoyaltyTransactionServiceImpl implements LoyaltyTransactionService {

    LoyaltyTransactionRepository loyaltyTransactionRepository;
    LoyaltyWalletRepository loyaltyWalletRepository;
    LoyaltyMapper loyaltyMapper;
    LoyaltyTierRepository tierBenefitRepository;

    private static final double AMOUNT_PER_POINT = 10000.0;

    @Override
    public List<TransactionHistoryResponse> getByCustomerId(UUID customerId) {
        List<LoyaltyTransaction> transactions = loyaltyTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(customerId);

        return transactions.stream()
                .map(loyaltyMapper::toTransactionHistoryResponse)
                .toList();
    }

    /*** MANAGE POINTS ***/

    @Override
    @Transactional
    public EarnPointsResponse deductPoints(DeductPointsRequest request) {
        LoyaltyWallet wallet = loyaltyWalletRepository
                .findByUserIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseThrow(() -> new AppException(ErrorCode.LOYALTY_WALLET_NOT_FOUND));

        int currentPoints = wallet.getLoyaltyCurrentPoint();

        if (currentPoints < request.getPointsToDeduct()) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        int balanceBefore = currentPoints;
        int balanceAfter = balanceBefore - request.getPointsToDeduct();
        wallet.setLoyaltyCurrentPoint(balanceAfter);
        loyaltyWalletRepository.save(wallet);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .userId(request.getCustomerId())
                .franchiseId(request.getFranchiseId())
                .promotionId(UUID.fromString(request.getOrderId()))
                .points(-request.getPointsToDeduct())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(LoyaltyTransactionType.REDEEM)
                .createdAt(Instant.now())
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toEarnPointsResponse(transaction, wallet.getCustomerLoyaltyTier().name());
    }

    @Override
    @Transactional
    public EarnPointsResponse earnPoints(EarnPointsRequest request) {

        int pointsEarned = (int) (request.getOrderAmount() / AMOUNT_PER_POINT);

        if (pointsEarned <= 0) {
            throw new AppException(ErrorCode.ORDER_AMOUNT_TOO_SMALL);
        }

        LoyaltyWallet wallet = loyaltyWalletRepository
                .findByUserIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseGet(() -> LoyaltyWallet.builder()
                        .userId(request.getCustomerId())
                        .franchiseId(request.getFranchiseId())
                        .loyaltyCurrentPoint(0)
                        .loyaltyTotalPoint(0)
                        .customerLoyaltyTier(CustomerLoyaltyTier.BRONZE)
                        .build());

        int balanceBefore = wallet.getLoyaltyCurrentPoint();
        int balanceAfter = balanceBefore + pointsEarned;
        int totalPointsAfter = wallet.getLoyaltyTotalPoint() + pointsEarned;

        wallet.setLoyaltyCurrentPoint(balanceAfter);
        wallet.setLoyaltyTotalPoint(totalPointsAfter);

        CustomerLoyaltyTier newTier = determineTier(totalPointsAfter);
        wallet.setCustomerLoyaltyTier(newTier);

        loyaltyWalletRepository.save(wallet);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .userId(request.getCustomerId())
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
        boolean alreadyRefunded = loyaltyTransactionRepository.existsByUserIdAndPromotionIdAndType(
                request.getCustomerId(),
                UUID.fromString(request.getOrderId()),
                LoyaltyTransactionType.REFUND
        );

        if (alreadyRefunded) {
                throw new AppException(ErrorCode.ORDER_ALREADY_REFUNDED);
        }

        LoyaltyWallet wallet = loyaltyWalletRepository
                .findByUserIdAndFranchiseId(request.getCustomerId(), request.getFranchiseId())
                .orElseThrow(() -> new AppException(ErrorCode.LOYALTY_WALLET_NOT_FOUND));

        int balanceBefore = wallet.getLoyaltyCurrentPoint();
        int balanceAfter = balanceBefore + request.getPointsToRefund();
        wallet.setLoyaltyCurrentPoint(balanceAfter);

        loyaltyWalletRepository.save(wallet);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .userId(request.getCustomerId())
                .franchiseId(request.getFranchiseId())
                .promotionId(UUID.fromString(request.getOrderId()))
                .points(request.getPointsToRefund())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(LoyaltyTransactionType.REFUND)
                .createdAt(Instant.now())
                .build();

        transaction = loyaltyTransactionRepository.save(transaction);

        return loyaltyMapper.toEarnPointsResponse(transaction, wallet.getCustomerLoyaltyTier().name());
    }
}