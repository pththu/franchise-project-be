package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.request.EarnPointsRequest;
import com.franchiseproject.loyaltyservice.dto.request.RedeemPromotionRequest;
import com.franchiseproject.loyaltyservice.dto.response.EarnPointsResponse;
import com.franchiseproject.loyaltyservice.dto.response.RedeemPromotionResponse;
import com.franchiseproject.loyaltyservice.dto.response.TransactionHistoryResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;

import java.util.List;
import java.util.UUID;

public interface LoyaltyTransactionService {
    List<TransactionHistoryResponse> getByCustomerId(UUID customerId);
    RedeemPromotionResponse redeemPromotion(RedeemPromotionRequest request);
    EarnPointsResponse earnPoints(EarnPointsRequest request);
}