package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.request.DeductPointsRequest;
import com.franchiseproject.loyaltyservice.dto.request.EarnPointsRequest;
import com.franchiseproject.loyaltyservice.dto.request.RefundPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.EarnPointsResponse;
import com.franchiseproject.loyaltyservice.dto.response.TransactionHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface LoyaltyTransactionService {
    List<TransactionHistoryResponse> getByCustomerId(UUID customerId);
    EarnPointsResponse deductPoints(DeductPointsRequest request);
    EarnPointsResponse earnPoints(EarnPointsRequest request);
    EarnPointsResponse refundPoints(RefundPointsRequest request);
}