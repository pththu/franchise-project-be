package com.franchiseproject.loyaltyservice.service;

import com.franchiseproject.loyaltyservice.dto.request.AdjustPointsRequest;
import com.franchiseproject.loyaltyservice.dto.response.AdjustPointsResponse;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;

import java.util.List;
import java.util.UUID;

public interface LoyaltyTransactionService {
    List<LoyaltyTransaction> getByCustomerId(UUID customerId);

    AdjustPointsResponse adjustPoints(AdjustPointsRequest request);
}